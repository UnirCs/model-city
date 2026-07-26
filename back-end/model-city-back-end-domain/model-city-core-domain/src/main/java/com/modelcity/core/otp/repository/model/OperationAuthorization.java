package com.modelcity.core.otp.repository.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "operation_authorizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationAuthorization {

    @Id
    @Column(name = "operation_authorization_id")
    private UUID operationAuthorizationId;

    @Column(name = "operation_type", nullable = false)
    private String operationType;

    @Column(name = "resource_type", nullable = false)
    private String resourceType;

    @Column(name = "resource_id", nullable = false)
    private String resourceId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OperationStatus status;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    /**
     * Irreversible HMAC of the verified DNI bound to this challenge, extracted from the verification
     * token. Null for operations that don't require DNI verification. Propagated to the service that
     * performs the authorized operation so it can deduplicate by DNI.
     */
    @Column(name = "dni_hash", length = 64)
    private String dniHash;

    @Column(name = "attempts_remaining", nullable = false)
    private int attemptsRemaining;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}

