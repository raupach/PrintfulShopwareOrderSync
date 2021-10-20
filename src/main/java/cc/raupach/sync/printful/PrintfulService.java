package cc.raupach.sync.printful;


import cc.raupach.sync.config.PrintfulSyncProperties;
import cc.raupach.sync.printful.dto.*;
import cc.raupach.sync.shopware.bo.AddressBo;
import cc.raupach.sync.shopware.bo.OrderBo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PrintfulService {

    @Autowired
    private PrintfulHttpClient printfulHttpClient;

    @Autowired
    private PrintfulSyncProperties printfulSyncProperties;

    public List<Order> getOrders() {
        OrderResponse orderRequest = printfulHttpClient.getOrders();
        log.info("Get {} Printful orders: {}",orderRequest.getResult().size(), orderRequest);
        return orderRequest.getResult();
    }


    public Optional<Order> getOrder(String orderNumber) {

        NewOrderResponse orderRequest = printfulHttpClient.getOrderByExternalId(orderNumber);
        if (orderRequest != null) {
            return Optional.of(orderRequest.getResult());
        } else {
            return Optional.empty();
        }
    }

    public void placeNewOrders(List<OrderBo> shopwareOrders) {
        shopwareOrders.forEach(orderBo -> {
// TODO Hausnummer prüfen !!!
            NewOrderRequest printfulOrder = NewOrderRequest.builder()
                    .external_id(orderBo.getOrderNumber())
                    .recipient(Address.builder()
                            .name(getName(orderBo.getDeliverAddress()))
                            .address1(orderBo.getDeliverAddress().getStreet())
                            .address2(orderBo.getDeliverAddress().getAdditionalAddressLine1())
                            .city(orderBo.getDeliverAddress().getCity())
                            .company(orderBo.getDeliverAddress().getCompany())
                            .country_code(orderBo.getDeliverAddress().getCountryCode())
                            .country_name(orderBo.getDeliverAddress().getCountryName())
                            .phone(orderBo.getDeliverAddress().getPhoneNumber())
                            .zip(orderBo.getDeliverAddress().getZipcode())
                            .build())
                    .items(orderBo.getProducts().stream()
                            .map(productBo -> Item.builder()
                                    .sync_variant_id(new BigInteger(productBo.getProductNumber()))
                                    .quantity(productBo.getQuantity())
                                    .retail_price(productBo.getPrice().toString())
                                    .name(productBo.getName())
                                    .build())
                            .collect(Collectors.toList()))
                    .packing_slip(OrderPackingSlip.builder()
                            .email(printfulSyncProperties.getCustomerServiceEmail())
                            .build())
                    .build();

            NewOrderResponse response = printfulHttpClient.postOrder(printfulOrder);
            log.info("Printful Order created: {}", response.getResult().getId());

        });

    }

    private String getName(AddressBo deliverAddress) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(deliverAddress.getTitle())) {
            sb.append(deliverAddress.getTitle()).append(" ");
        }

        if (StringUtils.isNotEmpty(deliverAddress.getFirstName())) {
            sb.append(deliverAddress.getFirstName()).append(" ");
        }

        sb.append(deliverAddress.getLastName());

        return sb.toString();
    }

}
