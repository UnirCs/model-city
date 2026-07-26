package com.modelcity.gateway.filter;

import com.modelcity.common.observability.model.CorrelationId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Entry point for log correlation in the microservices topology. Reuses the incoming
 * {@link CorrelationId#HEADER} header when present, otherwise generates a new id, and propagates
 * it both downstream (mutated request) and back to the caller (response header).
 *
 * Runs before the security filters so the id exists for the whole exchange.
 */
@Slf4j
@Component
public class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {

    public static final int FILTER_ORDER = -120;

    @Override
    public int getOrder() {
        return FILTER_ORDER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String incoming = exchange.getRequest().getHeaders().getFirst(CorrelationId.HEADER);
        String correlationId = CorrelationId.resolveOrCreate(incoming);

        log.debug("Correlation id for {} → {}", exchange.getRequest().getPath(), correlationId);

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(CorrelationId.HEADER, correlationId)
                .build();
        exchange.getResponse().getHeaders().set(CorrelationId.HEADER, correlationId);

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }
}
