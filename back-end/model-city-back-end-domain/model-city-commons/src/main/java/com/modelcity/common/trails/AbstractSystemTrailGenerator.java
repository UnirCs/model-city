package com.modelcity.common.trails;

import com.modelcity.common.observability.model.CorrelationId;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Base for the per-vertical {@code SystemTrailGenerator}s. Builds the common event envelope
 * (id, timestamp, correlation id from the MDC), serializes the payload and persists it through
 * the vertical's {@link SystemTrailStore}. Recording runs inside the caller's transaction.
 */
@Slf4j
public abstract class AbstractSystemTrailGenerator {

    /** Marker used as responsible user for events with no authenticated actor (e.g. Stripe webhooks). */
    public static final String SYSTEM_ACTOR = "SYSTEM";

    private final SystemTrailStore store;
    private final ObjectMapper objectMapper;

    protected AbstractSystemTrailGenerator(SystemTrailStore store, ObjectMapper objectMapper) {
        this.store = store;
        this.objectMapper = objectMapper;
    }

    protected void record(String eventType, OperationType operationType,
                          String responsibleUserId, String responsibleUserRole,
                          Long neighbourhoodId, Long zoneId,
                          String resourceType, String resourceId, Object payload) {
        String json = serialize(eventType, payload);
        NewSystemTrail event = new NewSystemTrail(
                UUID.randomUUID(), eventType, operationType, OffsetDateTime.now(),
                MDC.get(CorrelationId.MDC_KEY), responsibleUserId, responsibleUserRole,
                neighbourhoodId, zoneId, resourceType, resourceId, json);
        store.save(event);
        log.debug("Recorded system trail type={} resource={}:{} by={}", eventType, resourceType, resourceId, responsibleUserId);
    }

    /** Builds an ordered payload map from {@code key, value, key, value, ...} pairs (null values allowed). */
    protected static Map<String, Object> payload(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    private String serialize(String eventType, Object payload) {
        if (payload == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException e) {
            log.error("Failed to serialize payload for system trail type {}", eventType, e);
            return null;
        }
    }
}
