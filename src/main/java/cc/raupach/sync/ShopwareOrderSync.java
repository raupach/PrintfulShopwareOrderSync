package cc.raupach.sync;

import cc.raupach.sync.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableScheduling
public class ShopwareOrderSync {


    public static void main(String[] args) {

        log.info("Start.............................................................................................");
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        OrderSyncService syncService = context.getBean("orderSyncService", OrderSyncService.class);
        syncService.run();

    }

}
