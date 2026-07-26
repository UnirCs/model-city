package com.modelcity.common.config;

import com.modelcity.common.security.XAuthSubFilterReactive;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.WebFilter;

/**
 * Registers the {@link XAuthSubFilterReactive} WebFilter in reactive topologies (e.g. the gateway),
 * so the Auth0 {@code sub} claim is propagated downstream as the {@code X-Auth-Sub} header without
 * each application declaring the filter itself.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass({WebFilter.class, JwtAuthenticationToken.class})
public class XAuthSubFilterReactiveAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public XAuthSubFilterReactive xAuthSubFilterReactive() {
        return new XAuthSubFilterReactive();
    }
}
