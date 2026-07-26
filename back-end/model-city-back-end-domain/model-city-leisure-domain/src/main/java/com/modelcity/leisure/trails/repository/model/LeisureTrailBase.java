package com.modelcity.leisure.trails.repository.model;

import com.modelcity.common.trails.OperationType;
import com.modelcity.common.trails.SystemTrailView;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Topology-invariant mapping for the leisure audit-log entry. The concrete {@code @Entity LeisureTrail} lives
 * in the per-topology domain library; the monolith flavour adds real {@code @ManyToOne} navigations to
 * {@code User} / {@code Neighbourhood} / {@code Zone} on top of the plain id columns kept here.
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class LeisureTrailBase implements SystemTrailView {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 10)
    private OperationType operationType;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "responsible_user_id", length = 128)
    private String responsibleUserId;

    @Column(name = "responsible_user_role", length = 50)
    private String responsibleUserRole;

    @Column(name = "neighbourhood_id")
    private Long neighbourhoodId;

    @Column(name = "zone_id")
    private Long zoneId;

    @Column(name = "resource_type", nullable = false, length = 100)
    private String resourceType;

    @Column(name = "resource_id", length = 255)
    private String resourceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb")
    private String payload;
}
