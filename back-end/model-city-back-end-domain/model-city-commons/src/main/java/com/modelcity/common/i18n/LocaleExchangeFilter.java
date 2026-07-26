package com.modelcity.common.i18n;

import com.modelcity.common.observability.filter.CorrelationIdExchangeFilter;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

/**
 * {@link org.springframework.web.reactive.function.client.WebClient} filter that propagates the
 * current request locale to downstream calls as the {@code Accept-Language} header, keeping the
 * resolved language consistent across microservice hops (e.g. {@code CoreClient} → {@code http://core/...}).
 *
 * Mirrors {@link CorrelationIdExchangeFilter}.
 */
public final class LocaleExchangeFilter {

    private LocaleExchangeFilter() {
    }

    /** Builds the propagation filter; add it to every {@code WebClient.Builder}. */
    public static ExchangeFilterFunction create() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            String locale = SupportedLocale.from(LocaleContextHolder.getLocale()).code();
            ClientRequest mutated = ClientRequest.from(request)
                    .headers(headers -> headers.set(HttpHeaders.ACCEPT_LANGUAGE, locale))
                    .build();
            return Mono.just(mutated);
        });
    }
}
