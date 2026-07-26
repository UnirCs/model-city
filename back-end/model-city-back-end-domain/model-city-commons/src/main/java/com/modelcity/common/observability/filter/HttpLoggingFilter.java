package com.modelcity.common.observability.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Logs the full HTTP request and response (method, URI, query, headers, body, status and elapsed
 * time) at DEBUG for every endpoint. When DEBUG is disabled the request passes through untouched,
 * so there is no body-buffering cost in production (default level INFO).
 * <p>
 * Bodies are logged verbatim and are not redacted, so DEBUG must not be enabled in environments
 * handling sensitive data (OTP, tokens, Stripe payloads, PII). See docs/OBSERVABILITY.md.
 */
@Slf4j
public class HttpLoggingFilter extends OncePerRequestFilter {

    /**
     * Upper bound for the cached request body (bytes); larger bodies are truncated when logged.
     */
    private static final int MAX_CACHED_BODY_BYTES = 1024 * 1024;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!log.isDebugEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, MAX_CACHED_BODY_BYTES);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        logRequest(wrappedRequest);

        long start = System.nanoTime();
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            logResponse(wrappedResponse, wrappedRequest, elapsedMs);
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        String query = request.getQueryString() == null ? "" : "?" + request.getQueryString();
        log.debug("HTTP request  {} {}{} headers=[{}]",
                request.getMethod(),
                request.getRequestURI(),
                query,
                requestHeaders(request));
    }

    private void logResponse(ContentCachingResponseWrapper response, ContentCachingRequestWrapper request, long elapsedMs) {
        log.debug("HTTP response status={} ({} ms) requestBody=[{}] responseBody=[{}]",
                response.getStatus(),
                elapsedMs,
                body(request.getContentAsByteArray(), request.getCharacterEncoding()),
                body(response.getContentAsByteArray(), response.getCharacterEncoding()));
    }

    private String requestHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
                .map(name -> name + "=" + request.getHeader(name))
                .collect(Collectors.joining(", "));
    }

    private String body(byte[] content, String encoding) {
        if (content == null || content.length == 0) return "";
        try {
            return new String(content, encoding == null ? StandardCharsets.UTF_8.name() : encoding);
        } catch (IOException e) {
            return new String(content, StandardCharsets.UTF_8);
        }
    }
}
