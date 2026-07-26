package com.modelcity.common.config;

import com.modelcity.common.client.CoreClient;
import com.modelcity.common.security.ModelCityAccessAspect;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

/** Registers the {@link CoreClient} and the {@link ModelCityAccessAspect} beans. */
@AutoConfiguration(after = WebClientConfig.class)
@ConditionalOnClass({WebClient.class, Aspect.class})
public class ModelCityAccessAutoConfiguration {

    @Bean
    public CoreClient coreClient(WebClient.Builder webClientBuilder) {
        return new CoreClient(webClientBuilder);
    }

    @Bean
    public ModelCityAccessAspect modelCityAccessAspect(CoreClient coreClient) {
        return new ModelCityAccessAspect(coreClient);
    }
}

