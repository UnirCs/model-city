package com.modelcity.common.exception;

import java.time.Instant;

/** Standard error response body returned by all Model City microservices. */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String path,
        String message
) {}

