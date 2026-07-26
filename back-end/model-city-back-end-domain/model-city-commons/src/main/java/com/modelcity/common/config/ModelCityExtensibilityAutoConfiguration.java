package com.modelcity.common.config;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import com.modelcity.common.extensibility.ModelCityDisabledIfInheritedProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Registers the {@link ModelCityDisabledIfInheritedProcessor} so any application importing the platform
 * libraries honours {@link ModelCityDisabledIfInherited} on the component-scanned {@code Default*} beans.
 * This is the single piece of extensibility plumbing left: migrated verticals no longer ship an
 * {@code @AutoConfiguration} catalog of fallbacks.
 */
@AutoConfiguration
public class ModelCityExtensibilityAutoConfiguration {

    /** {@code static}: a {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor} must not drag its declaring configuration into early instantiation. */
    @Bean
    public static ModelCityDisabledIfInheritedProcessor modelCityDisabledIfInheritedProcessor() {
        return new ModelCityDisabledIfInheritedProcessor();
    }
}
