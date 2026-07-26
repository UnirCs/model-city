package com.modelcity.core.users.facade;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/** Wraps Auth0 Management API calls: create user, assign role and generate password ticket. */
@Slf4j
@Component
public class Auth0ManagementFacade {

    private final WebClient webClient;

    @Value("${auth0.domain}")
    private String domain;

    @Value("${auth0.management.client-id}")
    private String clientId;

    @Value("${auth0.management.client-secret}")
    private String clientSecret;

    @Value("${auth0.management.db-connection}")
    private String dbConnection;

    // Simple token cache
    private volatile String cachedToken;
    private volatile Instant tokenExpiresAt = Instant.EPOCH;

    public Auth0ManagementFacade(@Qualifier("externalWebClientBuilder") WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    /** Creates an invited user in Auth0 and returns their Auth0 user_id. */
    public String createUser(String email, String name) {
        String token = getManagementToken();
        Map<String, Object> body = Map.of(
                "email", email,
                "name", name,
                "connection", dbConnection,
                "email_verified", false,
                "verify_email", false,
                "password", UUID.randomUUID() + "Aa1!",
                "app_metadata", Map.of("invited", true, "user_type", "staff")
        );

        Map<?, ?> response = webClient.post()
                .uri("https://{domain}/api/v2/users", domain)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String userId = (String) response.get("user_id");
        log.info("Auth0 user created: userId={} email={}", userId, email);
        return userId;
    }

    /** Assigns the given Auth0 role IDs to a user. */
    public void assignRoles(String userId, List<String> roleIds) {
        String token = getManagementToken();
        webClient.post()
                .uri("https://{domain}/api/v2/users/{userId}/roles", domain, userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("roles", roleIds))
                .retrieve()
                .bodyToMono(Void.class)
                .block();

        log.info("Auth0 roles {} assigned to userId={}", roleIds, userId);
    }

    /** Returns the role names assigned to a user in Auth0. */
    public List<String> getUserRoles(String userId) {
        String token = getManagementToken();
        List<?> response = webClient.get()
                .uri("https://{domain}/api/v2/users/{userId}/roles", domain, userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(List.class)
                .block();

        if (response == null || response.isEmpty()) {
            return List.of();
        }
        return response.stream()
                .map(r -> (Map<?, ?>) r)
                .map(r -> (String) r.get("name"))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /** Generates a password change ticket URL so the invited user can set their password. */
    public String createPasswordChangeTicket(String userId) {
        String token = getManagementToken();
        Map<String, Object> body = Map.of(
                "user_id", userId,
                "client_id", clientId,
                "ttl_sec", 86400,
                "mark_email_as_verified", true);

        Map<?, ?> response = webClient.post()
                .uri("https://{domain}/api/v2/tickets/password-change", domain)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String ticket = (String) response.get("ticket");
        log.debug("Password change ticket created for userId={}", userId);
        return ticket;
    }

    private synchronized String getManagementToken() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiresAt)) {
            return cachedToken;
        }

        Map<String, String> body = Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "audience", "https://" + domain + "/api/v2/",
                "grant_type", "client_credentials",
                "scope", "read:users read:roles create:users update:users read:role_members create:user_tickets"
        );

        Map<?, ?> response = webClient.post()
                .uri("https://{domain}/oauth/token", domain)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        cachedToken = (String) response.get("access_token");
        int expiresIn = (int) response.get("expires_in");
        tokenExpiresAt = Instant.now().plusSeconds(expiresIn - 60); // 60s margin
        log.debug("Auth0 management token refreshed, expires in {}s", expiresIn);
        return cachedToken;
    }
}

