package com.modelcity.common.observability.filter;

import com.modelcity.common.observability.model.CorrelationId;
import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

/**
 * {@link WebClient} filter that propagates the current correlation id to downstream calls. Reads
 * {@link CorrelationId#MDC_KEY} from the MDC (populated by {@link CorrelationIdServletFilter} on the
 * calling servlet thread) and adds the {@link CorrelationId#HEADER} header to the outgoing request,
 * keeping the same id across microservice hops (e.g. {@code CoreClient} → {@code http://core/...}).
 */
public final class CorrelationIdExchangeFilter {

    private CorrelationIdExchangeFilter() {
    }

    /** Builds the propagation filter; add it to every {@code WebClient.Builder} used for blocking calls. */
    public static ExchangeFilterFunction create() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            String correlationId = MDC.get(CorrelationId.MDC_KEY);
            if (correlationId == null || correlationId.isBlank()) {
                return reactor.core.publisher.Mono.just(request);
            }
            ClientRequest mutated = ClientRequest.from(request)
                    .header(CorrelationId.HEADER, correlationId)
                    .build();
            return reactor.core.publisher.Mono.just(mutated);
        });
    }
}
