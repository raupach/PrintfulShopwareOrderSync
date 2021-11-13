package cc.raupach.sync.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@Setter
@Getter
@PropertySource("classpath:application.properties")
public class ShopwareSyncProperties {

    @Value("${sync.shopware.url}")
    private String url;

    @Value("${sync.shopware.orderOpenStateName}")
    private String orderOpenStateName;

    @Value("${sync.shopware.orderPaidStateName}")
    private String orderPaidStateName;

    @Value("${sync.shopware.orderProgressStateName}")
    private String orderProgressStateName;
}
