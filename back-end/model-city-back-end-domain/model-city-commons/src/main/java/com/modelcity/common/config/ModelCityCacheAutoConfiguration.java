package com.modelcity.common.config;

import com.modelcity.common.config.cache.CacheNames;
import com.modelcity.common.config.cache.ModelCityCacheProperties;
import com.modelcity.common.config.cache.PageJacksonModule;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

/**
 * Shared cache auto-configuration backed by Valkey through the Redis protocol (Spring Data Redis, Lettuce
 * client). Activated only when {@code modelcity.cache.enabled=true}; otherwise neither {@code @EnableCaching}
 * nor any {@code CacheManager} is registered, so the caching annotations across the modules stay inert.
 *
 * <p>The Valkey/Redis {@link RedisConnectionFactory} is provided by Spring Boot's standard Redis
 * auto-configuration (driven by {@code spring.data.redis.*}); this class only contributes the
 * {@link RedisCacheManager} with per-cache TTLs and JSON value serialization.
 */
@Slf4j
@AutoConfiguration
@EnableCaching
@ConditionalOnClass(RedisConnectionFactory.class)
@ConditionalOnProperty(prefix = "modelcity.cache", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(ModelCityCacheProperties.class)
public class ModelCityCacheAutoConfiguration {

    /** Long TTL caches: master/reference data that changes rarely. */
    private static final String[] LONG_TTL_CACHES = {
        CacheNames.CITY_PLACE, CacheNames.CITY_PLACES, CacheNames.CITY_ROUTE, CacheNames.CITY_ROUTES,
        CacheNames.CITY_ROUTE_PLACES, CacheNames.PUBLIC_SPACE, CacheNames.PUBLIC_SPACES,
        CacheNames.RESERVABLE_RESOURCES, CacheNames.USER_CARS, CacheNames.SANCTION,
        CacheNames.USER_SANCTIONS
    };

    /** Medium TTL caches: user-scoped data that can change but tolerates short staleness. */
    private static final String[] MEDIUM_TTL_CACHES = {
        CacheNames.PUBLIC_QUESTION, CacheNames.PUBLIC_QUESTIONS, CacheNames.USER_PROFILE,
        CacheNames.USER_LIST, CacheNames.CITIZEN_EXISTS
    };

    /** Short TTL caches: more dynamic listings. */
    private static final String[] SHORT_TTL_CACHES = {
        CacheNames.EVENT, CacheNames.EVENTS, CacheNames.SECURITY_ALERTS
    };

    private static final Duration LONG_TTL = Duration.ofMinutes(60);
    private static final Duration MEDIUM_TTL = Duration.ofMinutes(15);
    private static final Duration SHORT_TTL = Duration.ofMinutes(5);

    @Bean
    @ConditionalOnMissingBean(name = "cacheManager")
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory, ModelCityCacheProperties properties) {

        SerializationPair<Object> valueSerializer = SerializationPair.fromSerializer(jsonSerializer());

        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(properties.getDefaultTtl())
                .disableCachingNullValues()
                .serializeKeysWith(SerializationPair.fromSerializer(StringRedisSerializer.UTF_8))
                .serializeValuesWith(valueSerializer);

        Map<String, RedisCacheConfiguration> perCache = new LinkedHashMap<>();
        registerTtls(perCache, defaults, LONG_TTL_CACHES, LONG_TTL, properties);
        registerTtls(perCache, defaults, MEDIUM_TTL_CACHES, MEDIUM_TTL, properties);
        registerTtls(perCache, defaults, SHORT_TTL_CACHES, SHORT_TTL, properties);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(perCache)
                .transactionAware()
                .build();
    }

    /**
     * JSON serializer for cache values with default typing so concrete types (and {@code Page<T>} content)
     * round-trip. {@code allowIfBaseType(Object.class)} keeps it permissive for the internally-trusted
     * cache payloads while still going through a {@link PolymorphicTypeValidator}.
     */
    private GenericJacksonJsonRedisSerializer jsonSerializer() {
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        return GenericJacksonJsonRedisSerializer.builder()
                .enableDefaultTyping(typeValidator)
                .customize(mapperBuilder -> mapperBuilder
                        .findAndAddModules()
                        .addModule(new PageJacksonModule()))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(CachingConfigurer.class)
    public CachingConfigurer cachingConfigurer() {
        return new CachingConfigurer() {
            @Override
            public CacheErrorHandler errorHandler() {
                return new ResilientCacheErrorHandler();
            }
        };
    }

    /**
     * Swallows cache GET errors (e.g. stale entries written with a different serializer format) by evicting
     * the bad key and letting the call fall through to the real method. PUT/EVICT/CLEAR errors are only
     * logged so a transient Redis failure never breaks a write path.
     */
    private static final class ResilientCacheErrorHandler implements CacheErrorHandler {

        @Override
        public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
            log.warn("Cache GET error on '{}' key='{}': {} — evicting entry", cache.getName(), key, e.getMessage());
            try {
                cache.evict(key);
            } catch (RuntimeException evictEx) {
                log.warn("Failed to evict bad cache entry '{}' key='{}': {}", cache.getName(), key, evictEx.getMessage());
            }
        }

        @Override
        public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
            log.warn("Cache PUT error on '{}' key='{}': {}", cache.getName(), key, e.getMessage());
        }

        @Override
        public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
            log.warn("Cache EVICT error on '{}' key='{}': {}", cache.getName(), key, e.getMessage());
        }

        @Override
        public void handleCacheClearError(RuntimeException e, Cache cache) {
            log.warn("Cache CLEAR error on '{}': {}", cache.getName(), e.getMessage());
        }
    }

    private void registerTtls(
            Map<String, RedisCacheConfiguration> target,
            RedisCacheConfiguration base,
            String[] cacheNames,
            Duration groupTtl,
            ModelCityCacheProperties properties) {
        for (String cacheName : cacheNames) {
            Duration ttl = properties.getTtls().getOrDefault(cacheName, groupTtl);
            target.put(cacheName, base.entryTtl(ttl));
        }
    }
}
