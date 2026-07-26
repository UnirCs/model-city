package com.modelcity.mobility.sanctions.repository.model;

import com.modelcity.mobility.sanctions.store.model.SanctionView;
import jakarta.persistence.Column;
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
 * Topology-invariant mapping for a sanction: every column identical in the monolith and the microservices.
 * The concrete {@code @Entity Sanction} lives in the per-topology domain library and adds only the divergent
 * part — the monolith's real read-only {@code @ManyToOne} navigation to {@code User} (the issuing agent).
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class SanctionBase implements SanctionView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_plate", nullable = false, length = 32)
    private String licensePlate;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "image_base64", nullable = false, columnDefinition = "TEXT")
    private String imageBase64;

    @Column(name = "agent_sub", nullable = false)
    private String agentSub;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
