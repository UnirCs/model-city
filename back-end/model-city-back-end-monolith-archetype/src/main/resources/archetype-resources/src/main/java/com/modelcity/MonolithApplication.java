package com.modelcity;

import com.modelcity.common.config.ModelCityAccessAutoConfiguration;
import com.modelcity.common.config.WebClientConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

/**
 * Single entry point for the Model City monolith.
 * Bundles every microservice vertical (core, engagement, transaction-authorization,
 * leisure, mobility) in one JVM, plus the Auth0 OAuth2 resource server layer that the
 * standalone gateway provides in the microservices deployment.
 *
 * Auto-configurations from {@code model-city-commons} that wire the inter-microservice
 * WebClient (load-balanced) and the remote CoreClient are excluded because the monolith
 * replaces them with in-process beans.
 */
@SpringBootApplication(
        scanBasePackages = "com.modelcity",
        exclude = { WebClientConfig.class, ModelCityAccessAutoConfiguration.class }
)
@EnableJpaRepositories(basePackages = "com.modelcity")
@EntityScan(basePackages = "com.modelcity")
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class MonolithApplication {

    public static void main(String[] args) {
        String profile = System.getenv("PROFILE");
        if (profile != null && System.getProperty("spring.profiles.active") == null) {
            System.setProperty("spring.profiles.active", profile);
        }
        SpringApplication.run(MonolithApplication.class, args);
    }
}


