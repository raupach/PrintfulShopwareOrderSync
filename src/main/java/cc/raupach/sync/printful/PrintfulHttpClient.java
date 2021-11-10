package cc.raupach.sync.printful;

import cc.raupach.sync.config.PrintfulSyncProperties;
import cc.raupach.sync.printful.dto.NewOrderRequest;
import cc.raupach.sync.printful.dto.NewOrderResponse;
import cc.raupach.sync.printful.dto.OrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@Slf4j
public class PrintfulHttpClient {

    public static final String ORDERS = "orders";
    private static final String CONFIRM = "/confirm";

    @Autowired
    private PrintfulSyncProperties printfulSyncProperties;

    @Autowired
    @Qualifier("printful")
    private WebClient printfulWebClient;

    public OrderResponse getOrders() {

        return printfulWebClient.get()
                .uri(printfulSyncProperties.getUrl() + ORDERS)
                .header("Authorization", "Basic " + Base64Utils.encodeToString(printfulSyncProperties.getApiKey().getBytes(UTF_8)))
                .retrieve()
                .bodyToMono(OrderResponse.class)
                .block();
    }

    public NewOrderResponse getOrderByExternalId(String externalId) {

        Mono<NewOrderResponse> newOrderResponseMono = printfulWebClient.get()
                .uri(printfulSyncProperties.getUrl() + ORDERS + "/@" + externalId)
                .header("Authorization", "Basic " + Base64Utils.encodeToString(printfulSyncProperties.getApiKey().getBytes(UTF_8)))
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        return response.bodyToMono(NewOrderResponse.class);
                    } else {
                        return Mono.empty();
                    }
                });

        return newOrderResponseMono.block();
    }

    public NewOrderResponse postOrder(NewOrderRequest printfulOrder) {
        Mono<NewOrderResponse> result = printfulWebClient.post()
                .uri(printfulSyncProperties.getUrl() + ORDERS)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Basic " + Base64Utils.encodeToString(printfulSyncProperties.getApiKey().getBytes(UTF_8)))
                .body(Mono.just(printfulOrder), NewOrderRequest.class)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        return response.bodyToMono(NewOrderResponse.class);
                    } else {
                        return Mono.empty();
                    }
                });

        NewOrderResponse payload = result.block();
        if (payload == null) {
            throw new RuntimeException("Error....");
        } else if (payload.getCode() != 200) {
            throw new RuntimeException("Error...."+payload.getCode());
        } else {
            return payload;
        }
    }

    public NewOrderResponse confirmOrder(String orderId) {
        Mono<NewOrderResponse> result = printfulWebClient.post()
                .uri(printfulSyncProperties.getUrl() + ORDERS+ "/"+ orderId+ CONFIRM)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Basic " + Base64Utils.encodeToString(printfulSyncProperties.getApiKey().getBytes(UTF_8)))
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        return response.bodyToMono(NewOrderResponse.class);
                    } else {
                        return Mono.empty();
                    }
                });

        NewOrderResponse payload = result.block();
        if (payload == null) {
            throw new RuntimeException("Error....");
        } else if (payload.getCode() != 200) {
            throw new RuntimeException("Error...."+payload.getCode());
        } else {
            return payload;
        }
    }

}
