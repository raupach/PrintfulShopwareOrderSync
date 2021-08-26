package cc.raupach.sync.shopware;

import cc.raupach.sync.config.ShopwareSyncProperties;
import cc.raupach.sync.shopware.bo.*;
import cc.raupach.sync.shopware.dto.*;
import cc.raupach.sync.shopware.dto.attributes.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ShopwareService {

    @Autowired
    private ShopwareHttpClient shopwareHttpClient;

    @Autowired
    private ShopwareSyncProperties shopwareSyncProperties;


    private List<StateMachineBo> states = new ArrayList<>();

    public List<StateMachineBo> getStateMachines() {

        if (states.isEmpty()) {
            List<EntityData<StateMachine>> stateMachines = shopwareHttpClient.getStateMachines();
            states = stateMachines.stream().map(m -> StateMachineBo.builder()
                    .id(m.getId())
                    .name(m.getAttributes().getName())
                    .technicalName(m.getAttributes().getTechnicalName())
                    .states(shopwareHttpClient.getStateMachinesStates(m.getId()).stream()
                            .map(s -> StateMachineStateBo.builder()
                                    .id(s.getId())
                                    .name(s.getAttributes().getName())
                                    .technicalName(s.getAttributes().getTechnicalName())
                                    .build()).collect(Collectors.toList())
                    )
                    .build()).collect(Collectors.toList());

            log.info("{} StateMachines read.", states.size());
        }
        return states;

    }

    private List<EntityData<Order>> searchOrdersByState(String stateName) {

        String openStateId = findState(getStateMachines(), "order.state", stateName);

        FilteredRequest request = FilteredRequest.builder()
                .filter(List.of(Filter.builder()
                        .type(FilterType.equals)
                        .field("stateId")
                        .value(openStateId)
                        .build()))
                .build();

        List<EntityData<Order>> response = shopwareHttpClient.searchOrder(request);
        log.info("{} open orders found.", response.size());
        return response;
    }

    private String findState(List<StateMachineBo> stateMachines, String stateMachineName, String stateMachineStateName) {
        return stateMachines.stream()
                .filter(stateMachineBo -> StringUtils.equals(stateMachineBo.getTechnicalName(), stateMachineName))
                .findAny()
                .orElseThrow()
                .getStates().stream()
                .filter(s->StringUtils.equals(s.getTechnicalName(), stateMachineStateName))
                .findAny()
                .orElseThrow()
                .getId();
    }

    public List<OrderBo> getOpenAndPaidOrders() {
        List<EntityData<Order>> openOrders = searchOrdersByState(shopwareSyncProperties.getOrderOpenStateName());
        return mapToOrderBo(filterPayedOrders(openOrders));
    }

    public List<OrderBo> getInProgressAndPayedOrders() {
        List<EntityData<Order>> openOrders = searchOrdersByState(shopwareSyncProperties.getOrderProgressStateName());
        return mapToOrderBo(filterPayedOrders(openOrders));
    }


    private List<EntityData<Order>> filterPayedOrders(List<EntityData<Order>> orders) {
        String paidStateId = findState(getStateMachines(), "order_transaction.state", shopwareSyncProperties.getOrderPaidStateName());

        return orders.stream()
                .filter(order -> {
                    List<EntityData<OrderTransaction>> tx = shopwareHttpClient.getOrderTransactions(order.getId());
                    if (tx.size() != 1) {
                        throw new RuntimeException("Unexpected result");
                    } else {
                        return StringUtils.equals(paidStateId, tx.get(0).getAttributes().getStateId());
                    }
                }).collect(Collectors.toList());
    }

    public List<OrderBo> mapToOrderBo(List<EntityData<Order>> openOrders) {
        return openOrders.stream()
                .map(order -> {

                    List<EntityData<Addresses>> addresses = shopwareHttpClient.getAddresses(order.getId());
                    if (addresses.size() != 1) {
                        throw new RuntimeException("Multiple delivery Addresses");
                    }
                    EntityData<Addresses> address = addresses.get(0);
                    List<EntityData<Country>> countries = shopwareHttpClient.getCountryForAddress(address.getId());
                    if (addresses.size() != 1) {
                        throw new RuntimeException("Multiple delivery countries");
                    }
                    EntityData<Country> country = countries.get(0);

                    List<EntityData<LineItem>> lineItems = shopwareHttpClient.getLineItems(order.getId());
                    List<ProductBo> productBos = lineItems.stream()
                            .map(lineItem -> {
                                List<EntityData<Product>> products = shopwareHttpClient.getOrderLineItemProduct(lineItem.getId());
                                if (products.size() != 1) {
                                    throw new RuntimeException("Multiple products for one line item.");
                                }
                                EntityData<Product> product = products.get(0);

                                return ProductBo.builder()
                                        .quantity(lineItem.getAttributes().getQuantity())
                                        .productNumber(product.getAttributes().getProductNumber())
                                        .price(lineItem.getAttributes().getTotalPrice())
                                        .name(lineItem.getAttributes().getLabel())
                                        .build();
                            })
                            .collect(Collectors.toList());

                    return OrderBo.builder()
                            .orderId(order.getId())
                            .orderNumber(order.getAttributes().getOrderNumber())
                            .amountTotal(order.getAttributes().getAmountTotal())
                            .products(productBos)
                            .deliverAddress(AddressBo.builder()
                                    .addressId(address.getId())
                                    .firstName(address.getAttributes().getFirstName())
                                    .city(address.getAttributes().getCity())
                                    .lastName(address.getAttributes().getLastName())
                                    .street(address.getAttributes().getStreet())
                                    .title(address.getAttributes().getTitle())
                                    .zipcode(address.getAttributes().getZipcode())
                                    .additionalAddressLine1(address.getAttributes().getAdditionalAddressLine1())
                                    .additionalAddressLine2(address.getAttributes().getAdditionalAddressLine2())
                                    .phoneNumber(address.getAttributes().getPhoneNumber())
                                    .countryCode(country.getAttributes().getIso())
                                    .countryName(country.getAttributes().getName())
                                    .build())
                            .build();
                })
                .collect(Collectors.toList());

    }

    public void setOrderStatus(OrderBo order, OrderState orderState) {
        Transition transition = Transition.builder()
                .sendMail(true)
                .mediaIds(List.of())
                .documentIds(List.of())
                .build();

        shopwareHttpClient.setOrderState(transition, order.getOrderId(), orderState);
    }

    public void setDeliveryStatus(OrderBo order, DeliveryState deliveryState) {
        Transition transition = Transition.builder()
                .sendMail(true)
                .mediaIds(List.of())
                .documentIds(List.of())
                .build();

        shopwareHttpClient.setOrderState(transition, order.getOrderId(), deliveryState);
    }
}
