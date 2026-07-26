package com.modelcity.common.extensibility;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Startup hook that enforces {@link ModelCityDisabledIfInherited}: it runs after every bean definition has
 * been collected (component scan, city {@code @Configuration} classes, auto-configurations) but before any
 * bean is instantiated, and removes the definition of each annotated platform default whose seam is covered
 * by another bean definition.
 *
 * <p>For each annotated default it resolves the seam(s) — the most derived
 * {@link ModelCityExtensionPoint} types in the default's hierarchy, or the default class itself when it has
 * none — and looks for another bean definition assignable to that seam. Candidates are matched by their
 * <em>declared</em> type without instantiating anything, so a city bean registered either by component
 * scanning or by a {@code @Bean} method (declared as the seam type or more specific) is detected. An
 * annotated ancestor of the inspected default is not counted as an override, so a chain of annotated
 * defaults resolves to the most derived one regardless of registration order.
 *
 * <p>A plain {@link BeanFactoryPostProcessor} (not a {@code BeanDefinitionRegistryPostProcessor}) on
 * purpose: those run after all registry post-processors, guaranteeing the full set of definitions is
 * visible. Registered by {@code ModelCityExtensibilityAutoConfiguration}.
 */
@Slf4j
public class ModelCityDisabledIfInheritedProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (!(beanFactory instanceof BeanDefinitionRegistry registry)) {
            log.warn("Bean factory {} is not a BeanDefinitionRegistry; @ModelCityDisabledIfInherited defaults cannot back off",
                    beanFactory.getClass().getName());
            return;
        }
        for (String name : beanFactory.getBeanDefinitionNames()) {
            Class<?> type = resolveType(beanFactory, name);
            if (type == null || !type.isAnnotationPresent(ModelCityDisabledIfInherited.class)) {
                continue;
            }
            for (Class<?> seam : seamsOf(type)) {
                String override = findOverride(beanFactory, name, type, seam);
                if (override != null) {
                    registry.removeBeanDefinition(name);
                    log.info("Model City default '{}' ({}) disabled: seam {} is covered by bean '{}'",
                            name, type.getSimpleName(), seam.getSimpleName(), override);
                    break;
                }
            }
        }
    }

    /** Declared bean type, resolved without instantiating the bean (nor any {@code FactoryBean}). */
    private static Class<?> resolveType(ConfigurableListableBeanFactory beanFactory, String name) {
        try {
            return beanFactory.getType(name, false);
        } catch (BeansException ex) {
            return null;
        }
    }

    /**
     * The most derived {@link ModelCityExtensionPoint} types in the default's hierarchy; the class itself
     * when there is none (pure disabled-if-subclassed semantics).
     */
    private static List<Class<?>> seamsOf(Class<?> defaultType) {
        Set<Class<?>> annotated = new LinkedHashSet<>();
        collectSeams(defaultType, annotated);
        List<Class<?>> mostDerived = annotated.stream()
                .filter(seam -> annotated.stream().noneMatch(other -> other != seam && seam.isAssignableFrom(other)))
                .toList();
        return mostDerived.isEmpty() ? List.of(defaultType) : mostDerived;
    }

    private static void collectSeams(Class<?> type, Set<Class<?>> out) {
        for (Class<?> itf : type.getInterfaces()) {
            if (itf.isAnnotationPresent(ModelCityExtensionPoint.class)) {
                out.add(itf);
            }
            collectSeams(itf, out);
        }
        Class<?> superclass = type.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            if (superclass.isAnnotationPresent(ModelCityExtensionPoint.class)) {
                out.add(superclass);
            }
            collectSeams(superclass, out);
        }
    }

    /**
     * Name of a bean definition (other than the default itself) covering the seam, or {@code null}. An
     * annotated ancestor of the default is skipped: it is a less derived default of the same seam, not an
     * override of this one.
     */
    private static String findOverride(ConfigurableListableBeanFactory beanFactory, String defaultName,
                                       Class<?> defaultType, Class<?> seam) {
        for (String candidate : beanFactory.getBeanNamesForType(seam, true, false)) {
            if (candidate.equals(defaultName)) {
                continue;
            }
            Class<?> candidateType = resolveType(beanFactory, candidate);
            if (candidateType == null || candidateType == defaultType) {
                continue;
            }
            boolean annotatedAncestor = candidateType.isAssignableFrom(defaultType)
                    && candidateType.isAnnotationPresent(ModelCityDisabledIfInherited.class);
            if (annotatedAncestor) {
                continue;
            }
            return candidate;
        }
        return null;
    }
}
