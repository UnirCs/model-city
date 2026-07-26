package com.modelcity.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Optional;

/**
 * Servlet counterpart of {@link XAuthSubFilterReactive}: extracts the Auth0 {@code sub} claim from
 * the validated JWT in the {@link SecurityContextHolder} and exposes it to downstream layers via the
 * {@link AuthConstants#HEADER_AUTH_SUB} request header (which controllers consume through
 * {@code @RequestHeader(AuthConstants.HEADER_AUTH_SUB)}).
 *
 * Must run after Spring Security's authentication filters so the JWT has already been validated.
 * Not a {@code @Component}: servlet topologies register it manually in their security configuration
 * to avoid double registration as a servlet filter.
 */
@Slf4j
public class XAuthSubFilterServlet extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<String> sub = AuthClaimsExtractor.extractSub(auth);
        if (sub.isPresent()) {
            log.debug("Injecting {} header for sub={}", AuthConstants.HEADER_AUTH_SUB, sub.get());
            filterChain.doFilter(new SubHeaderRequestWrapper(request, sub.get()), response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    /** Wrapper that overrides the {@code X-Auth-Sub} header with the value from the JWT. */
    private static final class SubHeaderRequestWrapper extends HttpServletRequestWrapper {

        private final String sub;

        SubHeaderRequestWrapper(HttpServletRequest request, String sub) {
            super(request);
            this.sub = sub;
        }

        @Override
        public String getHeader(String name) {
            if (AuthConstants.HEADER_AUTH_SUB.equalsIgnoreCase(name)) return sub;
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (AuthConstants.HEADER_AUTH_SUB.equalsIgnoreCase(name)) return Collections.enumeration(Collections.singletonList(sub));
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Enumeration<String> names = super.getHeaderNames();
            java.util.List<String> all = new java.util.ArrayList<>();
            while (names.hasMoreElements()) {
                String n = names.nextElement();
                if (!AuthConstants.HEADER_AUTH_SUB.equalsIgnoreCase(n)) all.add(n);
            }
            all.add(AuthConstants.HEADER_AUTH_SUB);
            return Collections.enumeration(all);
        }
    }
}
