package com.modelcity.common.observability.filter;

import com.modelcity.common.observability.model.CorrelationId;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Establishes the {@link CorrelationId#MDC_KEY} MDC value for the lifetime of every request so
 * that all log lines emitted while serving it share a single correlation id.
 *
 * Reuses the incoming {@link CorrelationId#HEADER} header when present (set by the gateway or an
 * upstream service); otherwise generates a new id. The resolved id is echoed back on the response
 * header so callers can record it.
 *
 * Registered with {@code HIGHEST_PRECEDENCE} so it runs before Spring Security: even rejected
 * requests (401/403) are logged with a correlation id.
 */
public class CorrelationIdServletFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String correlationId = CorrelationId.resolveOrCreate(request.getHeader(CorrelationId.HEADER));
        MDC.put(CorrelationId.MDC_KEY, correlationId);
        response.setHeader(CorrelationId.HEADER, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CorrelationId.MDC_KEY);
        }
    }
}
