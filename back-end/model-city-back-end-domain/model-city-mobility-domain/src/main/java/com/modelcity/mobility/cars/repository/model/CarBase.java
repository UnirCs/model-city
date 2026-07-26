package com.modelcity.mobility.cars.repository.model;

import com.modelcity.mobility.cars.store.model.CarView;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

/**
 * Topology-invariant mapping for a car: every column identical in the monolith and the microservices. The
 * concrete {@code @Entity Car} lives in the per-topology domain library ({@code model-city-mobility-domain-monolith}
 * / {@code -microservices}) and adds only the part that differs — the monolith's real {@code @ManyToOne}
 * navigation to {@code User} (the owner), which the microservices flavour cannot have. This is the Phase-4
 * seam: the shared base lives once, the divergence in the subclass.
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class CarBase implements CarView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_sub", nullable = false)
    private String ownerSub;

    @Column(name = "license_plate", nullable = false, unique = true, length = 32)
    private String licensePlate;

    @Column(length = 128)
    private String nickname;

    @Column(length = 128)
    private String brand;

    @Column(length = 128)
    private String model;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}
