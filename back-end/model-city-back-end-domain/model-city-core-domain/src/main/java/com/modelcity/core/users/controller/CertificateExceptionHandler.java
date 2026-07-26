package com.modelcity.core.users.controller;

import com.modelcity.common.exception.ApiErrorResponse;
import com.modelcity.core.users.exception.CertificateExpiredException;
import com.modelcity.core.users.exception.CertificateNotVerifiedException;
import com.modelcity.core.users.exception.IncompleteCertificateException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class CertificateExceptionHandler {

    @ExceptionHandler(CertificateNotVerifiedException.class)
    public ResponseEntity<ApiErrorResponse> handleNotVerified(
            CertificateNotVerifiedException ex, HttpServletRequest request) {
        log.warn("Certificate not verified: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(error(400, "Bad Request", request, ex.getMessage()));
    }

    @ExceptionHandler(IncompleteCertificateException.class)
    public ResponseEntity<ApiErrorResponse> handleIncomplete(
            IncompleteCertificateException ex, HttpServletRequest request) {
        log.warn("Incomplete certificate: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(error(400, "Bad Request", request, ex.getMessage()));
    }

    @ExceptionHandler(CertificateExpiredException.class)
    public ResponseEntity<ApiErrorResponse> handleExpired(
            CertificateExpiredException ex, HttpServletRequest request) {
        log.warn("Expired certificate: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.GONE)
                .body(error(410, "Gone", request, ex.getMessage()));
    }

    private ApiErrorResponse error(int status, String error, HttpServletRequest request, String message) {
        return new ApiErrorResponse(Instant.now(), status, error, request.getRequestURI(), message);
    }
}
