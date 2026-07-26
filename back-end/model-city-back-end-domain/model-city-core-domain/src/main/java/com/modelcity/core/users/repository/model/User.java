package com.modelcity.core.users.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Citizen registered in Model City.
 * <p>
 * The primary key {@code id} is the {@code sub} claim from the Auth0 JWT
 * (format: {@code auth0|65f3a1b2c3d4e5f6}). It is the permanent identifier
 * assigned by Auth0 and never changes even if the user updates their email.
 * <p>
 * A citizen with multiple Auth0 accounts (e.g. Google + GitHub) will have
 * separate records — this is expected behaviour.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    /**
     * Primary key: Auth0 JWT {@code sub} claim.
     * Example: {@code auth0|65f3a1b2c3d4e5f6}
     */
    @Id
    @Column(name = "id", length = 128, nullable = false, updatable = false)
    private String id;

    /** Full name of the citizen (Auth0 {@code name} claim). */
    @Column(name = "name", length = 255)
    private String name;

    /**
     * Email address (Auth0 {@code email} claim). Unique across all citizens.
     * May be null if the {@code email} scope was not granted.
     */
    @Column(name = "email", length = 320, unique = true)
    private String email;

    /** Postal address of the citizen (filled in by the citizen in the app). */
    @Column(name = "address", length = 500)
    private String address;

    /**
     * Neighbourhood of residence.
     * Null until the citizen selects their neighbourhood in the app.
     * Loaded lazily — use only when the neighbourhood data is explicitly needed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "neighbourhood_id")
    private Neighbourhood neighbourhood;

    /** User role in the system. Persisted as dash-formatted string via {@link UserRoleConverter}. */
    @Column(name = "role", nullable = false, length = 50)
    private UserRole role;

    /** Account status. Disabled users keep their record but lose access. Defaults to ACTIVE. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * Irreversible HMAC of the citizen's DNI, captured the first time they verify an mTLS certificate.
     * Null until that first verification. Once set it locks this account to a single DNI: a later
     * verification with a different certificate (different DNI) is rejected, so one account cannot
     * vote using somebody else's certificate.
     */
    @Column(name = "dni_hash", length = 64)
    private String dniHash;

    /** UTC timestamp of the first sign-in (record creation). Immutable. */
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
        if (this.status == null) {
            this.status = UserStatus.ACTIVE;
        }
    }
}
