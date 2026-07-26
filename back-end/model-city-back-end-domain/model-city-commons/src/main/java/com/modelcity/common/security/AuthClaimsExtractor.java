package com.modelcity.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

/**
 * Shared helper that extracts the Auth0 {@code sub} claim from an authenticated JWT.
 * Keeps the validation logic in one place so the servlet monolith and the reactive gateway
 * reuse the same extraction rules even though they install the filter in different ways.
 */
public final class AuthClaimsExtractor {

    private AuthClaimsExtractor() {}

    /**
     * Returns the JWT {@code sub} claim when the authentication is a valid JWT.
     * The result is empty if the authentication is missing, not a JWT, not authenticated,
     * or if the subject is blank.
     */
    public static Optional<String> extractSub(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth && jwtAuth.isAuthenticated()) {
            String sub = jwtAuth.getToken().getSubject();
            if (sub != null && !sub.isBlank()) {
                return Optional.of(sub);
            }
        }
        return Optional.empty();
    }
}
