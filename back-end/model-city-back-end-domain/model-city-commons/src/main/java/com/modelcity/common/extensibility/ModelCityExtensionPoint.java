package com.modelcity.common.extensibility;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a type as a Model City <em>extension point</em> (a seam): the stable contract a local deployment
 * (a city) may override without editing platform code.
 *
 * <p>It is applied to the two kinds of seam in the {@code *-domain} libraries:
 * <ul>
 *   <li>a single-method <strong>use-case interface</strong> (e.g. {@code GetEventsUseCase}); and</li>
 *   <li>an <strong>abstract controller</strong> base class carrying {@code @RestController} and the
 *       class-level {@code @RequestMapping} (e.g. {@code EventController}).</li>
 * </ul>
 *
 * <p>The platform ships a {@code Default<Name>} implementation for each seam, registered as a fallback
 * {@code @Bean @ConditionalOnMissingBean} by the vertical's {@code @AutoConfiguration}. A city overrides a
 * seam by declaring its own bean of the seam type; the default then backs off. This annotation is the
 * type-level catalog of those seams and is the anchor the ArchUnit extensibility rules use to verify that
 * every {@code Default*} is wired through a seam and that overrides can only enter through one.
 *
 * <p>Breaking a seam (changing a use-case interface or an endpoint signature) is a <strong>MAJOR</strong>
 * change. Only interfaces and abstract classes may carry this annotation.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModelCityExtensionPoint {
}
