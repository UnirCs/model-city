package com.modelcity.common.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class XAuthSubFilterServletTest {

    private final XAuthSubFilterServlet filter = new XAuthSubFilterServlet();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Jwt jwtWithSub(String sub) {
        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(300),
                Map.of("alg", "none"), Map.of("sub", sub));
    }

    @Test
    void doFilterInternal_authenticatedJwt_injectsSubHeader() throws Exception {
        JwtAuthenticationToken token = new JwtAuthenticationToken(jwtWithSub("user-sub"), List.of());
        SecurityContextHolder.getContext().setAuthentication(token);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        var captor = org.mockito.ArgumentCaptor.forClass(jakarta.servlet.http.HttpServletRequest.class);
        verify(chain).doFilter(captor.capture(), any());
        assertThat(captor.getValue().getHeader(AuthConstants.HEADER_AUTH_SUB)).isEqualTo("user-sub");
    }

    @Test
    void doFilterInternal_noAuthentication_passesThroughUnmodified() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        var captor = org.mockito.ArgumentCaptor.forClass(jakarta.servlet.http.HttpServletRequest.class);
        verify(chain).doFilter(captor.capture(), any());
        assertThat(captor.getValue().getHeader(AuthConstants.HEADER_AUTH_SUB)).isNull();
    }

    @Test
    void headerWrapper_overridesExistingSubHeader() throws Exception {
        JwtAuthenticationToken token = new JwtAuthenticationToken(jwtWithSub("jwt-sub"), List.of());
        SecurityContextHolder.getContext().setAuthentication(token);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AuthConstants.HEADER_AUTH_SUB, "spoofed-sub");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        var captor = org.mockito.ArgumentCaptor.forClass(jakarta.servlet.http.HttpServletRequest.class);
        verify(chain).doFilter(captor.capture(), any());
        assertThat(captor.getValue().getHeader(AuthConstants.HEADER_AUTH_SUB)).isEqualTo("jwt-sub");
    }
}
