package com.modelcity.config;

import com.modelcity.common.observability.filter.CorrelationIdExchangeFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/** Replaces the load-balanced WebClient.Builder from commons with a plain one for external HTTP calls (Auth0, Stripe, etc.). */
@Configuration
public class MonolithWebClientConfig {

    private static final int MAX_IN_MEMORY_SIZE_BYTES = 1024 * 1024;

    /** Default WebClient.Builder bean (no load balancing — no Eureka in the monolith). */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE_BYTES))
                .filter(CorrelationIdExchangeFilter.create())
                .filter(errorHandlingFilter());
    }

    /** Alias bean kept for code that injects an "externalWebClientBuilder" qualifier. */
    @Bean("externalWebClientBuilder")
    public WebClient.Builder externalWebClientBuilder() {
        return webClientBuilder();
    }

    private ExchangeFilterFunction errorHandlingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            if (response.statusCode().isError()) {
                return response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(
                                new RuntimeException("HTTP error " + response.statusCode().value() + ": " + body)
                        ));
            }
            return Mono.just(response);
        });
    }
}

