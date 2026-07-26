package com.modelcity.common.observability.model;

import java.util.UUID;

/**
 * Shared constants and helpers for the Model City correlation id, the value used to correlate
 * every log line produced while serving a single request across layers and microservices.
 *
 * The id travels between services in the {@link #HEADER} HTTP header and is exposed to the
 * logging system through the {@link #MDC_KEY} MDC key.
 */
public final class CorrelationId {

    /** MDC key holding the correlation id; referenced from the log pattern as {@code %X{modelCityCorrelationId}}. */
    public static final String MDC_KEY = "modelCityCorrelationId";

    /** HTTP header that carries the correlation id between the gateway, the microservices and the monolith. */
    public static final String HEADER = "X-Model-City-Correlation-Id";

    private CorrelationId() {
    }

    /** Returns {@code incoming} when present, otherwise a freshly generated random id. */
    public static String resolveOrCreate(String incoming) {
        return (incoming != null && !incoming.isBlank()) ? incoming : UUID.randomUUID().toString();
    }
}
