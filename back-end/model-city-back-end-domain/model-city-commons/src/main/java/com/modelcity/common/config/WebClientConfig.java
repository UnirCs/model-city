package com.modelcity.common.config;

import com.modelcity.common.i18n.LocaleExchangeFilter;
import com.modelcity.common.observability.filter.CorrelationIdExchangeFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/** Provides pre-configured {@link WebClient.Builder} beans for HTTP calls. */
@Configuration
@ConditionalOnClass(WebClient.class)
public class WebClientConfig {

    private static final int MAX_IN_MEMORY_SIZE_BYTES = 1024 * 1024;

    /** Load-balanced WebClient builder for inter-microservice calls via Eureka. Default bean. */
    @Bean
    @LoadBalanced
    @ConditionalOnMissingBean(name = "webClientBuilder")
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE_BYTES))
                .filter(CorrelationIdExchangeFilter.create())
                .filter(LocaleExchangeFilter.create())
                .filter(errorHandlingFilter());
    }

    /** Standard WebClient builder for external HTTP calls (no load balancing). */
    @Bean("externalWebClientBuilder")
    @ConditionalOnMissingBean(name = "externalWebClientBuilder")
    public WebClient.Builder externalWebClientBuilder() {
        return WebClient.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE_BYTES))
                .filter(CorrelationIdExchangeFilter.create())
                .filter(LocaleExchangeFilter.create())
                .filter(errorHandlingFilter());
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

