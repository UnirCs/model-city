package com.modelcity.common.trails;

import java.time.OffsetDateTime;

/** Filter criteria for the admin system-trail read endpoints. Any field may be null (no filter). */
public record SystemTrailQuery(
        String eventType,
        String responsibleUserId,
        OffsetDateTime from,
        OffsetDateTime to
) {}
