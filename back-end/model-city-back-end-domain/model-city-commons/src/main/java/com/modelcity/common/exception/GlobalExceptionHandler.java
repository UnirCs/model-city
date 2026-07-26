package com.modelcity.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.stream.Collectors;

/** Centralised exception handler — returns {@link ApiErrorResponse} with no stack trace. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(
            ResponseStatusException ex, HttpServletRequest request) {
        HttpStatusCode statusCode = ex.getStatusCode();
        String errorPhrase = resolveReasonPhrase(statusCode);
        String message = ex.getReason() != null ? ex.getReason() : errorPhrase;
        log.warn("{} {} → {} {}", request.getMethod(), request.getRequestURI(), statusCode.value(), message);
        return ResponseEntity.status(statusCode).body(new ApiErrorResponse(
                Instant.now(), statusCode.value(), errorPhrase, request.getRequestURI(), message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("Validation failed at {}: {}", request.getRequestURI(), message);
        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                Instant.now(), 400, "Bad Request", request.getRequestURI(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.internalServerError().body(new ApiErrorResponse(
                Instant.now(), 500, "Internal Server Error", request.getRequestURI(),
                "An unexpected error occurred"));
    }

    private String resolveReasonPhrase(HttpStatusCode statusCode) {
        HttpStatus resolved = HttpStatus.resolve(statusCode.value());
        return resolved != null ? resolved.getReasonPhrase() : "Error";
    }
}

