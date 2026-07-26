package com.modelcity.common.security;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class XAuthSubFilterReactiveTest {

    private final XAuthSubFilterReactive filter = new XAuthSubFilterReactive();

    private Jwt jwtWithSub(String sub) {
        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(300),
                Map.of("alg", "none"), Map.of("sub", sub));
    }

    @Test
    void filter_authenticatedJwt_propagatesSubHeader() {
        JwtAuthenticationToken token = new JwtAuthenticationToken(jwtWithSub("user-sub"), List.of());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/foo"));
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(token))
                .block();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());
        assertThat(captor.getValue().getRequest().getHeaders().getFirst(AuthConstants.HEADER_AUTH_SUB))
                .isEqualTo("user-sub");
    }

    @Test
    void filter_noSecurityContext_passesThroughUnmodified() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/foo"));
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());
        assertThat(captor.getValue().getRequest().getHeaders().getFirst(AuthConstants.HEADER_AUTH_SUB)).isNull();
    }

    @Test
    void getOrder_isMinusNinety() {
        assertThat(filter.getOrder()).isEqualTo(-90);
    }
}
