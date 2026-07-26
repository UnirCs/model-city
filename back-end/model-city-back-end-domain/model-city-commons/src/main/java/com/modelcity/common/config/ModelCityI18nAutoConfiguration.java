package com.modelcity.common.config;

import com.modelcity.common.i18n.SupportedLocale;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/**
 * Configures locale negotiation for the servlet topologies (microservices + monolith). The
 * {@link LocaleResolver} reads {@code Accept-Language} and clamps it to the {@link SupportedLocale}
 * set (default {@code es}), so controllers can inject a resolved {@link java.util.Locale} and
 * {@link LocaleContextHolder} reflects the request locale across the use-case/adapter layers.
 *
 * Reactive apps such as the gateway do not pick this up (no Spring MVC on the classpath) and simply
 * forward the {@code Accept-Language} header downstream.
 */
@AutoConfiguration
@ConditionalOnClass(AcceptHeaderLocaleResolver.class)
public class ModelCityI18nAutoConfiguration {

    /** Accept-Language resolver restricted to the supported locales, defaulting to {@code es}. */
    @Bean(name = DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME)
    @ConditionalOnMissingBean(name = DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME)
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setSupportedLocales(SupportedLocale.locales());
        resolver.setDefaultLocale(SupportedLocale.DEFAULT.locale());
        return resolver;
    }

    /** Echoes the resolved locale back on the {@code Content-Language} response header. */
    @Bean
    public WebMvcConfigurer modelCityContentLanguageConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new HandlerInterceptor() {
                    @Override
                    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                        response.setHeader(HttpHeaders.CONTENT_LANGUAGE,
                                SupportedLocale.from(LocaleContextHolder.getLocale()).code());
                        return true;
                    }
                });
            }
        };
    }
}
