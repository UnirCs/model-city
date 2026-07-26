package com.modelcity.common.trails;

import com.fasterxml.jackson.annotation.JsonRawValue;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Response contract for the admin system-trail read endpoints. */
public record SystemTrailDto(
        UUID eventId,
        String eventType,
        OperationType operationType,
        OffsetDateTime occurredAt,
        String correlationId,
        String responsibleUserId,
        String responsibleUserRole,
        Long neighbourhoodId,
        Long zoneId,
        String resourceType,
        String resourceId,
        @JsonRawValue String payload
) {

    public static SystemTrailDto from(SystemTrailView v) {
        return new SystemTrailDto(
                v.getEventId(),
                v.getEventType(),
                v.getOperationType(),
                v.getOccurredAt(),
                v.getCorrelationId(),
                v.getResponsibleUserId(),
                v.getResponsibleUserRole(),
                v.getNeighbourhoodId(),
                v.getZoneId(),
                v.getResourceType(),
                v.getResourceId(),
                v.getPayload());
    }
}
