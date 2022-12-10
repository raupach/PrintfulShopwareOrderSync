package cc.raupach.sync.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
//@EnableWebSecurity
//@EnableWebFluxSecurity
public class WebClientConfig {

    @Value("${sync.shopware.token-uri}")
    private String tokenUri;

    @Value("${sync.shopware.client-id}")
    private String clientId;

    @Value("${sync.shopware.client-secret}")
    private String clientSecret;

    @Bean
    public ReactiveClientRegistrationRepository clientRegistrations() {

        ClientRegistration registration = ClientRegistration
          .withRegistrationId("sw")
          .tokenUri(tokenUri)
          .clientId(clientId)
          .clientSecret(clientSecret)
          .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
          .build();
        return new InMemoryReactiveClientRegistrationRepository(registration);
    }

    @Bean
    public DefaultReactiveOAuth2AuthorizedClientManager authorizedClientManager(ReactiveClientRegistrationRepository clientRegistrations, ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {

        ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
          .clientCredentials()
          .build();

        DefaultReactiveOAuth2AuthorizedClientManager authorizedClientManager = new DefaultReactiveOAuth2AuthorizedClientManager(clientRegistrations, authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Bean(name = "shopware")
    WebClient webClient(DefaultReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Client = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2Client.setDefaultClientRegistrationId("custom");

        return WebClient.builder()
          .filter(oauth2Client)
//          .apply(oauth2Client.oauth2AuthorizedClient)
          .exchangeStrategies(ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
            .build())
          .build();
    }

    @Bean(name = "printful")
    WebClient printfulWebClient() {

        return WebClient.builder()
          .build();
    }
}
