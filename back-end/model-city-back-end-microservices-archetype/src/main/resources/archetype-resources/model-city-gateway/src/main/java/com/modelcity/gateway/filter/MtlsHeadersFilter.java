package com.modelcity.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Propagates AWS ALB mTLS headers to downstream services.
 * When a request comes through the mTLS ALB, the ALB adds X-Amzn-Mtls-* headers.
 * This filter ensures those headers are forwarded to the microservices.
 */
@Component
public class MtlsHeadersFilter implements GlobalFilter, Ordered {

    private static final List<String> MTLS_HEADERS = List.of(
            "X-Amzn-Mtls-Clientcert-Subject",
            "X-Amzn-Mtls-Clientcert-Issuer",
            "X-Amzn-Mtls-Clientcert-Validity",
            "X-Amzn-Mtls-Clientcert-Leaf",
            "X-Amzn-Mtls-Clientcert-Serial-Number"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest.Builder mutatedRequest = request.mutate();

        // Propagate all mTLS headers present in the original request
        MTLS_HEADERS.forEach(headerName -> {
            List<String> headerValues = request.getHeaders().get(headerName);
            if (headerValues != null && !headerValues.isEmpty()) {
                mutatedRequest.header(headerName, headerValues.toArray(new String[0]));
            }
        });

        return chain.filter(exchange.mutate().request(mutatedRequest.build()).build());
    }

    @Override
    public int getOrder() {
        // Execute before security filters (-100) but after routing (-10000)
        return -50;
    }
}


