package com.modelcity.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Reactive counterpart of {@link XAuthSubFilterServlet}: propagates the Auth0 JWT {@code sub} claim
 * as the {@link AuthConstants#HEADER_AUTH_SUB} header to downstream services.
 *
 * Implemented as a plain {@link WebFilter} (WebFlux) instead of a Spring Cloud Gateway
 * {@code GlobalFilter} so commons stays free of gateway-specific dependencies. The order places it
 * after Spring Security's {@code WebFilterChainProxy} (default order {@code -100}) so the reactive
 * security context is populated before the {@code sub} claim is read.
 */
@Slf4j
public class XAuthSubFilterReactive implements WebFilter, Ordered {

    public static final int FILTER_ORDER = -90;

    @Override
    public int getOrder() {
        return FILTER_ORDER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Resolve the (possibly mutated) exchange as a plain value first, then call chain.filter(...)
        // exactly once via flatMap. Calling chain.filter(...) inside a branch and then relying on
        // switchIfEmpty(...) for the other branch is a trap: chain.filter(...) returns Mono<Void>,
        // which by definition never emits a value, so switchIfEmpty(...) would treat an
        // already-handled request as "empty" and invoke the downstream chain a second time.
        return ReactiveSecurityContextHolder.getContext()
                .mapNotNull(SecurityContext::getAuthentication)
                .map(auth -> resolveExchange(exchange, auth))
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }

    private ServerWebExchange resolveExchange(ServerWebExchange exchange, Authentication auth) {
        return AuthClaimsExtractor.extractSub(auth)
                .map(sub -> {
                    log.debug("Propagating JWT sub claim → sub={}", sub);
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header(AuthConstants.HEADER_AUTH_SUB, sub)
                            .build();
                    return exchange.mutate().request(mutatedRequest).build();
                })
                .orElse(exchange);
    }
}
