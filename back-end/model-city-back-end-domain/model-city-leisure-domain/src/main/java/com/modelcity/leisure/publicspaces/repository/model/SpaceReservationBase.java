package com.modelcity.leisure.publicspaces.repository.model;

import com.modelcity.leisure.publicspaces.model.SpaceReservationView;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Topology-invariant mapping for a space reservation. The concrete {@code @Entity SpaceReservation} lives in
 * the per-topology domain library and adds only the divergent part — the monolith's real {@code @ManyToOne}
 * navigation to {@code User} (the booking citizen), absent in microservices.
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class SpaceReservationBase implements SpaceReservationView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    @Column(name = "citizen_sub", nullable = false)
    private String citizenSub;

    @Column(name = "citizen_name")
    private String citizenName;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private Instant createdAt;
}
