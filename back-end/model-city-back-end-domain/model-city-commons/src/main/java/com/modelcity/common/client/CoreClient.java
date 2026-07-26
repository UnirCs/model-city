package com.modelcity.common.client;

import com.modelcity.common.dto.OperationAuthorizationResponseDto;
import com.modelcity.common.security.AuthConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.UUID;

/**
 * Pre-configured client to the core microservice.
 * Uses the load-balanced WebClient builder from {@code WebClientConfig} to resolve {@code http://core}.
 */
@Slf4j
@RequiredArgsConstructor
public class CoreClient {

    private final WebClient.Builder webClientBuilder;

    /** Returns the application role of the user identified by the given Auth0 sub. */
    public String getUserRole(String sub) {
        return readField(sub, "role");
    }

    /** Returns the display name of the user identified by the given Auth0 sub. */
    public String getUserName(String sub) {
        return readField(sub, "name");
    }

    private String readField(String sub, String field) {
        log.debug("CoreClient → GET /users/{} (field={})", sub, field);
        @SuppressWarnings("rawtypes")
        Map response = webClientBuilder.build()
                .get()
                .uri("http://core/users/{sub}", sub)
                .header(AuthConstants.HEADER_AUTH_SUB, sub)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        return response == null ? null : (String) response.get(field);
    }

    /** Fetches an operation authorization by id from the core microservice. */
    public OperationAuthorizationResponseDto getOperationAuthorization(UUID id) {
        log.debug("CoreClient → GET /operation-authorizations/{}", id);
        return webClientBuilder.build()
                .get()
                .uri("http://core/operation-authorizations/{id}", id)
                .retrieve()
                .bodyToMono(OperationAuthorizationResponseDto.class)
                .block();
    }

    /** Marks a VERIFIED authorization as BURNT once the authorized operation has completed. */
    public void burnOperationAuthorization(UUID id) {
        log.debug("CoreClient → PATCH /operation-authorizations/{}/burn", id);
        webClientBuilder.build()
                .patch()
                .uri("http://core/operation-authorizations/{id}/burn", id)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
