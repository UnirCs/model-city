package com.modelcity.core.trails.repository.model;

import com.modelcity.common.trails.OperationType;
import com.modelcity.common.trails.SystemTrailView;
import com.modelcity.core.users.repository.model.Neighbourhood;
import com.modelcity.core.users.repository.model.User;
import com.modelcity.core.users.repository.model.Zone;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Audit log entry for the core vertical (users + OTP). The entity is shared by both topologies,
 * like the rest of core's entities; {@code responsible_user_id}, {@code neighbourhood_id} and
 * {@code zone_id} are backed by real foreign keys (the core database owns those tables).
 */
@Entity
@Table(name = "core_trails")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoreTrail implements SystemTrailView {

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

    /** Read-only navigation to the actor that produced the event. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_user_id", insertable = false, updatable = false)
    private User responsibleUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "neighbourhood_id", insertable = false, updatable = false)
    private Neighbourhood neighbourhood;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", insertable = false, updatable = false)
    private Zone zone;
}
