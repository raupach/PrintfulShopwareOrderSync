package cc.raupach.sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShopwareOrderSync {


    public static void main(String[] args) {
        SpringApplication.run(ShopwareOrderSync.class, args);
    }
}
