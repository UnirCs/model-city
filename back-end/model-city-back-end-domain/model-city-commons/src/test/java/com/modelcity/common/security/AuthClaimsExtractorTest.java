package com.modelcity.common.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AuthClaimsExtractorTest {

    private Jwt jwtWithSub(String sub) {
        return new Jwt("token-value", Instant.now(), Instant.now().plusSeconds(300),
                Map.of("alg", "none"), sub == null ? Map.of("iss", "test") : Map.of("sub", sub));
    }

    @Test
    void extractSub_authenticatedJwt_returnsSubject() {
        JwtAuthenticationToken token = new JwtAuthenticationToken(jwtWithSub("user-sub"), List.of());

        Optional<String> result = AuthClaimsExtractor.extractSub(token);

        assertThat(result).contains("user-sub");
    }

    @Test
    void extractSub_notAuthenticated_returnsEmpty() {
        JwtAuthenticationToken token = new JwtAuthenticationToken(jwtWithSub("user-sub"), List.of());
        token.setAuthenticated(false);

        Optional<String> result = AuthClaimsExtractor.extractSub(token);

        assertThat(result).isEmpty();
    }

    @Test
    void extractSub_notAJwtToken_returnsEmpty() {
        Authentication other = new UsernamePasswordAuthenticationToken("user", "pass");

        Optional<String> result = AuthClaimsExtractor.extractSub(other);

        assertThat(result).isEmpty();
    }

    @Test
    void extractSub_nullAuthentication_returnsEmpty() {
        assertThat(AuthClaimsExtractor.extractSub(null)).isEmpty();
    }

    @Test
    void extractSub_blankSubject_returnsEmpty() {
        JwtAuthenticationToken token = new JwtAuthenticationToken(jwtWithSub(null), List.of());

        Optional<String> result = AuthClaimsExtractor.extractSub(token);

        assertThat(result).isEmpty();
    }
}
