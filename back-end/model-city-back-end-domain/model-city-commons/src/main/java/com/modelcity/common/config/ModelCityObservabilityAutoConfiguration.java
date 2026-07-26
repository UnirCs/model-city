package com.modelcity.common.config;

import com.modelcity.common.observability.filter.CorrelationIdServletFilter;
import com.modelcity.common.observability.filter.HttpLoggingFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Registers the servlet-side observability filters shared by the monolith and the microservices:
 * the {@link CorrelationIdServletFilter} (correlation id in the MDC) and the {@link HttpLoggingFilter}
 * (request/response logging at DEBUG). Reactive apps such as the gateway do not pick this up
 * (no {@link OncePerRequestFilter} on the classpath) and use their own correlation filter.
 */
@AutoConfiguration
@ConditionalOnClass(OncePerRequestFilter.class)
@ConditionalOnProperty(prefix = "modelcity.observability.correlation", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class ModelCityObservabilityAutoConfiguration {

    @Bean
    public FilterRegistrationBean<CorrelationIdServletFilter> correlationIdFilter() {
        FilterRegistrationBean<CorrelationIdServletFilter> registration = new FilterRegistrationBean<>(new CorrelationIdServletFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<HttpLoggingFilter> httpLoggingFilter() {
        FilterRegistrationBean<HttpLoggingFilter> registration = new FilterRegistrationBean<>(new HttpLoggingFilter());
        // Runs before Spring Security so the logged request reflects the headers as received.
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }
}
