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
public class PrintfulSyncProperties {

    @Value("${sync.printful.apiKey}")
    private String apiKey;

    @Value("${sync.printful.url}")
    private String url;

    @Value("${sync.printful.customerServiceEmail}")
    private String customerServiceEmail;

}
