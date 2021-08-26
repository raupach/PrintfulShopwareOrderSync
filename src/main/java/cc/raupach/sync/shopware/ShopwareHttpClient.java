package cc.raupach.sync.shopware;

import cc.raupach.sync.config.ShopwareSyncProperties;
import cc.raupach.sync.shopware.dto.*;
import cc.raupach.sync.shopware.dto.attributes.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
public class ShopwareHttpClient {


    private static final String STATE_MACHINE = "state-machine";
    private static final String SEARCH_ORDER = "search/order";
    private static final String STATES = "/states";
    private static final String ORDER = "order";
    private static final String ORDER_LINE_ITEM = "order-line-item";
    private static final String ORDER_ADDRESS = "order-address";
    private static final String ADDRESSES = "/addresses";
    private static final String LINE_ITEMS = "/line-items";
    private static final String PRODUCT = "/product";
    private static final String TRANSACTIONS = "/transactions";
    private static final String COUNTRY = "/country";
    private static final String ACTION_ORDER_STATE = "/_action/order/";
    private static final String ACTION_DELIVERY_STATE = "/_action/order_delivery/";
    private static final String STATE = "/state/";

    @Autowired
    @Qualifier("shopware")
    private WebClient shopwareWebClient;

    @Autowired
    private ShopwareSyncProperties shopwareSyncProperties;

    public List<EntityData<StateMachine>> getStateMachines() {
                EntityResponse<StateMachine> stateMachineEntityResponse = shopwareWebClient.get()
                .uri(shopwareSyncProperties.getUrl() + STATE_MACHINE)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<EntityResponse<StateMachine>>() {})
                .block();

        return stateMachineEntityResponse.getData();
    }

    public List<EntityData<StateMachineState>> getStateMachinesStates(String stateMachineId) {
        EntityResponse<StateMachineState> stateMachineStateEntityResponse = shopwareWebClient.get()
                .uri(shopwareSyncProperties.getUrl() + STATE_MACHINE+"/"+stateMachineId+STATES)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<EntityResponse<StateMachineState>>() {})
                .block();

        return stateMachineStateEntityResponse.getData();
    }

    public List<EntityData<Order>> searchOrder(FilteredRequest request) {
        Mono<EntityResponse<Order>> result = shopwareWebClient.post()
                .uri(shopwareSyncProperties.getUrl() + SEARCH_ORDER)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(request), FilteredRequest.class)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        return response.bodyToMono(new ParameterizedTypeReference<EntityResponse<Order>>() {
                        });
                    } else {
                        return Mono.empty();
                    }
                });

        EntityResponse<Order> payload = result.block();
        if (payload == null) {
            log.error("Error...");
            return null;
        } else {
            return payload.getData();
        }

    }

    public List<EntityData<Addresses>> getAddresses(String orderId) {
        EntityResponse<Addresses> addressesEntityResponse = shopwareWebClient.get()
                .uri(shopwareSyncProperties.getUrl() + ORDER+ "/" +orderId + ADDRESSES)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<EntityResponse<Addresses>>() {})
                .block();

        return addressesEntityResponse.getData();
    }

    public List<EntityData<LineItem>> getLineItems(String orderId) {
        EntityResponse<LineItem> lineItemEntityResponse = shopwareWebClient.get()
                .uri(shopwareSyncProperties.getUrl() + ORDER+ "/" +orderId + LINE_ITEMS)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<EntityResponse<LineItem>>() {})
                .block();

        return lineItemEntityResponse.getData();
    }

    public List<EntityData<Product>> getOrderLineItemProduct(String orderLineItemId) {
        EntityResponse<Product> productEntityResponse = shopwareWebClient.get()
                .uri(shopwareSyncProperties.getUrl() + ORDER_LINE_ITEM+ "/" +orderLineItemId + PRODUCT)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<EntityResponse<Product>>() {})
                .block();

        return productEntityResponse.getData();
    }

    public List<EntityData<OrderTransaction>> getOrderTransactions(String orderId) {
        EntityResponse<OrderTransaction> productEntityResponse = shopwareWebClient.get()
                .uri(shopwareSyncProperties.getUrl() + ORDER+ "/" +orderId + TRANSACTIONS)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<EntityResponse<OrderTransaction>>() {})
                .block();

        return productEntityResponse.getData();
    }

    public List<EntityData<Country>> getCountryForAddress(String addressId) {
        EntityResponse<Country> countryEntityResponse = shopwareWebClient.get()
                .uri(shopwareSyncProperties.getUrl() + ORDER_ADDRESS+ "/" +addressId + COUNTRY)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<EntityResponse<Country>>() {})
                .block();

        return countryEntityResponse.getData();
    }

    public void setOrderState(Transition transition, String orderId, OrderState state) {

        shopwareWebClient.post()
                .uri(shopwareSyncProperties.getUrl() + ACTION_ORDER_STATE + orderId + STATE + state)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(transition), Transition.class)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        log.info("Shopware order {} set status: {}", orderId, state);
                        return Mono.empty();
                    } else {
                        log.error("Error setting OrderState: " + response.statusCode().toString());
                        return Mono.error(new RuntimeException("Error setting OrderState: " + response.statusCode().toString()));
                    }
                })
                .block();

    }

    public void setOrderState(Transition transition, String orderId, DeliveryState state) {

        shopwareWebClient.post()
                .uri(shopwareSyncProperties.getUrl() + ACTION_DELIVERY_STATE + orderId + STATE + state)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(transition), Transition.class)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        log.info("Shopware order {} set delivery status: {}", orderId, state);
                        return Mono.empty();
                    } else {
                        log.error("Error setting OrderState: " + response.statusCode().toString());
                        return Mono.error(new RuntimeException("Error setting delivery State: " + response.statusCode().toString()));
                    }
                })
                .block();

    }
}
