package com.modelcity.common.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for operation authorization responses from the core microservice.
 * This is a public API contract shared across microservices.
 */
public record OperationAuthorizationResponseDto(
        UUID operationAuthorizationId,
        String operationType,
        String resourceType,
        String resourceId,
        String userId,
        Instant expiresAt,
        String status,
        int attemptsRemaining,
        Instant createdAt,
        /** Irreversible HMAC of the verified DNI bound to this authorization. Null if not DNI-bound. */
        String dniHash
) {}
