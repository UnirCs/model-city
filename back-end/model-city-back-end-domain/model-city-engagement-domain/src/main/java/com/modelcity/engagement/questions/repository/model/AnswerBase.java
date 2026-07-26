package com.modelcity.engagement.questions.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

/**
 * Topology-invariant mapping for a citizen's YES/NO vote. Only the {@code question} back-reference to the
 * (topology divergent) {@code CivicQuestion} diverges — declared on the concrete subclass in each topology
 * library — plus, in the monolith, an additive shadow {@code @ManyToOne} navigation to {@code User} (the
 * citizen), absent from the microservices flavour.
 *
 * <p>No view interface exists for {@code Answer}: the {@code AnswerStore} port returns only primitives
 * ({@code boolean}/{@code Long}), so this class needs no {@code implements}.
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class AnswerBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Auth0 sub claim propagated by the gateway via X-Auth-Sub header. Kept for the UI hint and audit. */
    @Column(name = "citizen_id", nullable = false, length = 128)
    private String citizenId;

    /** Irreversible HMAC of the voter's DNI. Deduplication key — no raw PII is stored. */
    @Column(name = "dni_hash", nullable = false, length = 64)
    private String dniHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Vote vote;

    @Column(name = "answered_at", nullable = false)
    private OffsetDateTime answeredAt;
}
