package cc.raupach.sync.printful;

import cc.raupach.sync.config.PrintfulSyncProperties;
import cc.raupach.sync.printful.dto.NewOrderRequest;
import cc.raupach.sync.printful.dto.NewOrderResponse;
import cc.raupach.sync.printful.dto.OrderResponse;
import cc.raupach.sync.printful.dto.VariantResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class PrintfulHttpClient {

    public static final String ORDERS = "orders";
    private static final String CONFIRM = "/confirm";
    private static final String STORE_VARIANTS = "store/variants";
    private static final String AUTHORIZATION = "Authorization";

    @Autowired
    private PrintfulSyncProperties printfulSyncProperties;

    @Autowired
    @Qualifier("printful")
    private WebClient printfulWebClient;


    private String getAuthorizationValue() {
        return "Bearer " + printfulSyncProperties.getAccessKey();
    }


    public OrderResponse getOrders() {

        return printfulWebClient.get()
            .uri(printfulSyncProperties.getUrl() + ORDERS)
            .header(AUTHORIZATION, getAuthorizationValue())
            .retrieve()
            .bodyToMono(OrderResponse.class)
            .block();
    }

    public NewOrderResponse getOrderByExternalId(String externalId) {

        Mono<NewOrderResponse> newOrderResponseMono = printfulWebClient.get()
            .uri(printfulSyncProperties.getUrl() + ORDERS + "/@" + externalId)
            .header(AUTHORIZATION, getAuthorizationValue())
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
            .header(AUTHORIZATION, getAuthorizationValue())
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
            throw new RuntimeException("Error...." + payload.getCode());
        } else {
            return payload;
        }
    }

    public NewOrderResponse confirmOrder(String orderId) {
        Mono<NewOrderResponse> result = printfulWebClient.post()
            .uri(printfulSyncProperties.getUrl() + ORDERS + "/" + orderId + CONFIRM)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, getAuthorizationValue())
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
            throw new RuntimeException("Error...." + payload.getCode());
        } else {
            return payload;
        }
    }


    public VariantResponse getVariantDetail(String id) {
        Mono<VariantResponse> responseMono = printfulWebClient.get()
            .uri(printfulSyncProperties.getUrl() + STORE_VARIANTS + "/" + id)
            .header(AUTHORIZATION, getAuthorizationValue())
            .exchangeToMono(response -> {
                if (response.statusCode().equals(HttpStatus.OK)) {
                    return response.bodyToMono(VariantResponse.class);
                } else {
                    return Mono.empty();
                }
            });

        return responseMono.block();
    }
}
