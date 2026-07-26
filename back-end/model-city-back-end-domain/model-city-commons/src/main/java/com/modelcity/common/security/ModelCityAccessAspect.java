package com.modelcity.common.security;

import com.modelcity.common.client.CoreClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Enforces {@link ModelCityAccess} role annotations on controller methods.
 * Reads the {@link AuthConstants#HEADER_AUTH_SUB} request header, fetches the caller role from core
 * and rejects the call with 403 if none of the declared roles matches.
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class ModelCityAccessAspect {

    private static final String ROLE_PLATFORM_ADMIN  = "MODEL-CITY-PLATFORM-ADMIN";
    private static final String ROLE_BACKOFFICE       = "MODEL-CITY-BACKOFFICE";
    private static final String ROLE_OPERATOR         = "MODEL-CITY-OPERATOR";
    private static final String ROLE_MOBILITY_AGENT   = "MODEL-CITY-MOBILITY-AGENT";

    private final CoreClient coreClient;

    @Before("@annotation(com.modelcity.common.security.ModelCityAccess.PlatformAdmin)"
            + " || @annotation(com.modelcity.common.security.ModelCityAccess.BackOffice)"
            + " || @annotation(com.modelcity.common.security.ModelCityAccess.Operator)"
            + " || @annotation(com.modelcity.common.security.ModelCityAccess.MobilityAgent)")
    public void checkAccess(JoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        List<String> allowed = collectAllowedRoles(method);

        String sub = extractAuthSub();
        if (sub == null || sub.isBlank()) {
            log.warn("Access denied to {}: missing {} header", method.getName(), AuthConstants.HEADER_AUTH_SUB);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Missing authentication");
        }

        String role = coreClient.getUserRole(sub);
        if (role == null || !allowed.contains(role)) {
            log.warn("Access denied to {}: sub={} role={} allowed={}", method.getName(), sub, role, allowed);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Required role: one of " + allowed);
        }
        log.debug("Access granted to {}: sub={} role={}", method.getName(), sub, role);
    }

    private List<String> collectAllowedRoles(Method method) {
        List<String> allowed = new ArrayList<>();
        if (method.isAnnotationPresent(ModelCityAccess.PlatformAdmin.class))  allowed.add(ROLE_PLATFORM_ADMIN);
        if (method.isAnnotationPresent(ModelCityAccess.BackOffice.class))     allowed.add(ROLE_BACKOFFICE);
        if (method.isAnnotationPresent(ModelCityAccess.Operator.class))       allowed.add(ROLE_OPERATOR);
        if (method.isAnnotationPresent(ModelCityAccess.MobilityAgent.class))  allowed.add(ROLE_MOBILITY_AGENT);
        return allowed;
    }

    private String extractAuthSub() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes servletAttrs)) return null;
        return servletAttrs.getRequest().getHeader(AuthConstants.HEADER_AUTH_SUB);
    }
}

