package cc.raupach.sync;


import cc.raupach.sync.printful.PrintfulService;
import cc.raupach.sync.printful.dto.Order;
import cc.raupach.sync.printful.dto.OrderStatus;
import cc.raupach.sync.printful.dto.Shipment;
import cc.raupach.sync.shopware.DeliveryState;
import cc.raupach.sync.shopware.OrderState;
import cc.raupach.sync.shopware.ShopwareService;
import cc.raupach.sync.shopware.bo.OrderBo;
import lombok.extern.slf4j.Slf4j;
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

        log.info(">>> Check open and paid orders. <<<");
        processOpenAndPaidShopwareOrders();

        log.info(">>> Check in progress orders. <<<");
        processInProgressOrders();
    }

    private void processInProgressOrders() {
        List<OrderBo> inProgressAndPayedOrders = shopwareService.getInProgressAndPayedOrders();
        log.info("{} in progress and paid orders found.", inProgressAndPayedOrders.size());

        inProgressAndPayedOrders.forEach(order -> {
            Optional<Order> printfulOrderOptional = printfulService.getOrder(order.getOrderNumber());

            printfulOrderOptional.ifPresentOrElse(printfulOrder -> {
                OrderStatus status = printfulOrder.getStatus();
                List<String> trackingUrls = printfulOrder.getShipments().stream().map(Shipment::getTracking_url).toList();
                switch (status) {
                    case fulfilled -> shopwareService.setDeliveryStatus(order, trackingUrls, DeliveryState.ship);
                    case canceled, failed, archived -> shopwareService.setOrderStatus(order, OrderState.cancel, true);
                    default -> log.info("No change for order {}", order.getOrderNumber());
                }
            }, () -> log.info("Ooops, 'in process' order {} is not a valid printful order.", order.getOrderNumber()));
        });
    }

    private void processOpenAndPaidShopwareOrders() {
        List<OrderBo> openAndPaidOrders = shopwareService.getOpenAndPaidOrders();
        log.info("{} open and paid orders found.", openAndPaidOrders.size());

        openAndPaidOrders.forEach(order -> {
            Optional<Order> printfulOrderOptional = printfulService.getOrder(order.getOrderNumber());

            printfulOrderOptional.ifPresentOrElse(printfulOrder -> {
                OrderStatus status = printfulOrder.getStatus();
                List<String> trackingUrls = printfulOrder.getShipments().stream().map(Shipment::getTracking_url).toList();
                switch (status) {
                    case inprocess -> shopwareService.setOrderStatus(order, OrderState.process, true);
                    case canceled, failed, archived -> shopwareService.setOrderStatus(order, OrderState.cancel, false);
                    case fulfilled -> {
                        shopwareService.setOrderStatus(order, OrderState.process, false);
                        shopwareService.setDeliveryStatus(order, trackingUrls, DeliveryState.ship);
                    }
                    case pending -> log.info("Order {} still pending.", order.getOrderNumber());
                    default -> {
                    }
                }
            }, () -> {
                // Create new Printful Order
                printfulService.placeNewOrders(List.of(order));
            });
        });
    }

}
