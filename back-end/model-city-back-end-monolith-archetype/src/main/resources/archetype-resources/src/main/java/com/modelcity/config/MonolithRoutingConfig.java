package com.modelcity.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.web.bind.annotation.RestController;

/**
 * Mounts every vertical under the public {@code /api/<vertical>} prefix so the external API
 * surface is identical to the microservices deployment, where the ALB prepends {@code /api}
 * and the Spring Cloud Gateway routes {@code /api/{serviceId}/**}. Exposed paths:
 * {@code /api/core/...}, {@code /api/engagement/...},
 * {@code /api/leisure/...}, {@code /api/mobility/...}.
 *
 * <p>The prefix is applied only to {@code @RestController} beans, so management endpoints
 * (e.g. {@code /actuator/health}) keep their bare paths and the ALB health check is unaffected.
 */
@Configuration
public class MonolithRoutingConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api/core", c ->
                isController(c) && c.getPackageName().startsWith("com.modelcity.core"));
        configurer.addPathPrefix("/api/engagement", c ->
                isController(c) && c.getPackageName().startsWith("com.modelcity.engagement"));
        configurer.addPathPrefix("/api/leisure", c ->
                isController(c) && c.getPackageName().startsWith("com.modelcity.leisure"));
        configurer.addPathPrefix("/api/mobility", c ->
                isController(c) && c.getPackageName().startsWith("com.modelcity.mobility"));
    }

    /**
     * Detects {@code @RestController} including when it is inherited from an abstract base controller
     * (the extension-point pattern: abstract base annotated {@code @RestController}, concrete subclass
     * registered as a default bean without re-declaring the annotation).
     */
    private boolean isController(Class<?> c) {
        return AnnotatedElementUtils.hasAnnotation(c, RestController.class);
    }
}

