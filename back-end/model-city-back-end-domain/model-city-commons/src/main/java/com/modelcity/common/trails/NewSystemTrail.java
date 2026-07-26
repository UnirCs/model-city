package com.modelcity.common.trails;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Command carrying a fully-built event envelope to the persistence port. {@code payload} is serialized JSON. */
public record NewSystemTrail(
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
        String payload
) {}
