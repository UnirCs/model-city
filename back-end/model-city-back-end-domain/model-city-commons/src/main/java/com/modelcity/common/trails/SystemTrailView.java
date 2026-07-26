package com.modelcity.common.trails;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Read-only view of a persisted system trail (audit log entry).
 * Each vertical's {@code @Entity} implements this interface so the shared read/query code
 * stays decoupled from the per-deployment JPA mapping.
 */
public interface SystemTrailView {

    UUID getEventId();

    String getEventType();

    OperationType getOperationType();

    OffsetDateTime getOccurredAt();

    String getCorrelationId();

    String getResponsibleUserId();

    String getResponsibleUserRole();

    Long getNeighbourhoodId();

    Long getZoneId();

    String getResourceType();

    String getResourceId();

    /** Event-specific body as raw JSON. May be null. */
    String getPayload();
}
