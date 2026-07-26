package com.modelcity.common.config.cache;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Global cache settings. When {@code modelcity.cache.enabled} is {@code false} (the default) the cache
 * auto-configuration is not activated and the {@code @Cacheable}/{@code @CacheEvict} annotations present in
 * the code become inert (Spring does not create the caching proxy).
 *
 * <p>The Valkey connection itself is configured through the standard {@code spring.data.redis.*} properties
 * (Valkey speaks the Redis protocol, so Spring Boot's Redis auto-configuration drives the connection).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "modelcity.cache")
public class ModelCityCacheProperties {

    /** Master switch. If {@code false}, caching annotations stay in the code but do nothing. */
    private boolean enabled = false;

    /** Default time-to-live applied to caches without a specific entry in {@link #ttls}. */
    private Duration defaultTtl = Duration.ofMinutes(30);

    /** Per-cache TTL overrides, keyed by cache name. Overrides the built-in defaults. */
    private Map<String, Duration> ttls = new LinkedHashMap<>();
}
