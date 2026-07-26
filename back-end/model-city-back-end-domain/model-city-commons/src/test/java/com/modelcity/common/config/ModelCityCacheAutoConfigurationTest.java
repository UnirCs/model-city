package com.modelcity.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.modelcity.common.config.cache.PageJacksonModule;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

/**
 * Verifies the global toggle behaviour of the cache auto-configuration and the {@link Page} JSON
 * round-trip, without requiring a running Valkey instance (the Lettuce connection is lazy).
 */
class ModelCityCacheAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ModelCityCacheAutoConfiguration.class));

    @Test
    void cacheDisabledByDefault_registersNoCacheManager() {
        runner.run(context -> assertThat(context).doesNotHaveBean(CacheManager.class));
    }

    @Test
    void cacheEnabled_registersRedisCacheManager() {
        runner.withPropertyValues("modelcity.cache.enabled=true")
                // Stands in for the connection factory Spring Boot's Redis auto-config provides at runtime.
                .withBean(LettuceConnectionFactory.class)
                .run(context -> assertThat(context).hasSingleBean(RedisCacheManager.class));
    }

    @Test
    void pageRoundTrip_preservesContentAndMetadata() {
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        GenericJacksonJsonRedisSerializer serializer = GenericJacksonJsonRedisSerializer.builder()
                .enableDefaultTyping(typeValidator)
                .customize(b -> b.findAndAddModules().addModule(new PageJacksonModule()))
                .build();

        Page<SampleDto> original = new PageImpl<>(
                List.of(new SampleDto(1L, "Plaza Mayor"), new SampleDto(2L, "Parque del Oeste")),
                PageRequest.of(0, 6),
                42L);

        byte[] bytes = serializer.serialize(original);
        Object restored = serializer.deserialize(bytes);

        assertThat(restored).isInstanceOf(Page.class);
        @SuppressWarnings("unchecked")
        Page<SampleDto> page = (Page<SampleDto>) restored;
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getSize()).isEqualTo(6);
        assertThat(page.getTotalElements()).isEqualTo(42L);
        assertThat(page.getContent()).containsExactly(
                new SampleDto(1L, "Plaza Mayor"), new SampleDto(2L, "Parque del Oeste"));
    }

    @Test
    void cacheEnabled_registersCachingConfigurer() {
        runner.withPropertyValues("modelcity.cache.enabled=true")
                .withBean(LettuceConnectionFactory.class)
                .run(context -> assertThat(context).hasSingleBean(CachingConfigurer.class));
    }

    @Test
    void resilientErrorHandler_getCacheError_evictsKeyAndDoesNotThrow() {
        runner.withPropertyValues("modelcity.cache.enabled=true")
                .withBean(LettuceConnectionFactory.class)
                .run(context -> {
                    CachingConfigurer configurer = context.getBean(CachingConfigurer.class);
                    CacheErrorHandler handler = configurer.errorHandler();
                    Cache cache = mock(Cache.class);
                    assertThatNoException().isThrownBy(() ->
                            handler.handleCacheGetError(new RuntimeException("stale data"), cache, "key1"));
                    verify(cache).evict("key1");
                });
    }

    @Test
    void resilientErrorHandler_putAndEvictErrors_doNotThrow() {
        runner.withPropertyValues("modelcity.cache.enabled=true")
                .withBean(LettuceConnectionFactory.class)
                .run(context -> {
                    CachingConfigurer configurer = context.getBean(CachingConfigurer.class);
                    CacheErrorHandler handler = configurer.errorHandler();
                    Cache cache = mock(Cache.class);
                    assertThatNoException().isThrownBy(() ->
                            handler.handleCachePutError(new RuntimeException("put fail"), cache, "key1", "value"));
                    assertThatNoException().isThrownBy(() ->
                            handler.handleCacheEvictError(new RuntimeException("evict fail"), cache, "key1"));
                    assertThatNoException().isThrownBy(() ->
                            handler.handleCacheClearError(new RuntimeException("clear fail"), cache));
                });
    }

    /** Minimal DTO standing in for the real module DTOs. */
    public record SampleDto(Long id, String name) {}
}
