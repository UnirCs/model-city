package com.modelcity.common.extensibility;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a component-scanned <em>platform default</em> bean that must back off when a local deployment
 * (a city) registers its own bean for the same seam.
 *
 * <p>This replaces the per-vertical {@code @AutoConfiguration} catalogs of
 * {@code @Bean @ConditionalOnMissingBean} fallbacks: a {@code Default*} class now carries a regular
 * stereotype ({@code @Service}, {@code @Component}, {@code @RestController}) plus this annotation, and is
 * registered by plain component scanning. At startup, before any bean is instantiated,
 * {@link ModelCityDisabledIfInheritedProcessor} removes the default's bean definition if another bean
 * definition covers the same seam.
 *
 * <p>The seam of a default is the nearest {@link ModelCityExtensionPoint} type in its hierarchy (a use-case
 * interface, an abstract controller base, a store port). If the class has no
 * {@code @ModelCityExtensionPoint} ancestor, the seam is the annotated class itself, so the default is
 * disabled exactly when a subclass of it is registered. A city override therefore keeps working through any
 * of the usual doors: {@code extends} the {@code Default*} class, {@code extends} the abstract controller
 * base, or {@code implements} the use-case/store interface.
 *
 * <p>The annotation is deliberately <strong>not</strong> {@link java.lang.annotation.Inherited @Inherited}:
 * a city subclass of a default does not become disableable by accident; it opts in by carrying the
 * annotation itself (in which case the chain resolves to the most derived bean).
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModelCityDisabledIfInherited {
}
