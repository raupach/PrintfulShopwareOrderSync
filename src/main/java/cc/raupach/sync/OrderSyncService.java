package cc.raupach.sync;


import cc.raupach.sync.printful.PrintfulService;
import cc.raupach.sync.printful.dto.Order;
import cc.raupach.sync.printful.dto.OrderStatus;
import cc.raupach.sync.shopware.DeliveryState;
import cc.raupach.sync.shopware.OrderState;
import cc.raupach.sync.shopware.ShopwareService;
import cc.raupach.sync.shopware.bo.OrderBo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class OrderSyncService {

    @Autowired
    private ShopwareService shopwareService;

    @Autowired
    private PrintfulService printfulService;


    public void run() {

        processOpenShopwareOrders();
        processInProgressOrders();
    }

    private void processInProgressOrders() {
        List<OrderBo> inProgressAndPayedOrders = shopwareService.getInProgressAndPayedOrders();
        log.info("{} in progress and paid orders found.", inProgressAndPayedOrders.size());

        inProgressAndPayedOrders.forEach(order -> {
            Optional<Order> printfulOrderOptional = printfulService.getOrder(order.getOrderNumber());

            printfulOrderOptional.ifPresentOrElse(printfulOrder -> {
                OrderStatus status = printfulOrder.getStatus();
                switch (status) {

                    case fulfilled:
                        shopwareService.setDeliveryStatus(order, DeliveryState.ship);
                        break;
                    case canceled:
                    case failed:
                    case archived:
                        shopwareService.setOrderStatus(order, OrderState.cancel, true);
                        break;
                    default:
                        break;
                }
            }, () -> {
                log.warn("Ooops, 'in process' order {} is missing at printful.", order.getOrderNumber());
            });
        });
    }

    private void processOpenShopwareOrders() {
        List<OrderBo> openAndPaidOrders = shopwareService.getOpenAndPaidOrders();
        log.info("{} open and paid orders found.", openAndPaidOrders.size());

        openAndPaidOrders.forEach(order -> {
            Optional<Order> printfulOrderOptional = printfulService.getOrder(order.getOrderNumber());

            printfulOrderOptional.ifPresentOrElse(printfulOrder -> {
                OrderStatus status = printfulOrder.getStatus();
                switch (status) {
                    case inprocess:
                        shopwareService.setOrderStatus(order, OrderState.process, true);
                        break;
                    case canceled:
                    case failed:
                    case archived:
                        shopwareService.setOrderStatus(order, OrderState.cancel, false);
                        break;
                    case pending:
                    default:
                        break;
                }
            }, () ->{
                // Create new Printful Order
                 printfulService.placeNewOrders(List.of(order));
            });
        });
    }

    private Optional<OrderBo> findShopwareOrder(List<OrderBo> shopwareOrders, Order printfulOrder) {
        return shopwareOrders.stream()
                .filter(o-> StringUtils.equals(printfulOrder.getExternal_id(), o.getOrderNumber()))
                .findAny();
    }
}
