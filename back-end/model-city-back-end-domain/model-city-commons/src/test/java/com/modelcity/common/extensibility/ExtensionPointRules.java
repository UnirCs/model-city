package com.modelcity.common.extensibility;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.Locale;
import java.util.Optional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Reusable ArchUnit rules that enforce the Model City extensibility contract on a vertical's classes. They
 * live in {@code model-city-commons} (test-jar) so every {@code *-domain} module applies the <em>same</em>
 * rules to its own package — see the per-vertical {@code *ExtensibilityArchTest} classes. Adding a new
 * vertical only requires copying that ~6-line test.
 *
 * <p>The rules guard two promises of the model:
 * <ul>
 *   <li><strong>Defaults are city-neutral</strong> — no platform class carries a city name ({@link #NO_CITY_NAMES}).</li>
 *   <li><strong>Overrides can only enter through a seam</strong> — every {@code Default*} fallback is wired
 *       through a {@link ModelCityExtensionPoint} interface or abstract base, or — for a
 *       {@code Default*Repository} — through a {@code @NoRepositoryBean} generic repository, the
 *       Spring-Data-native equivalent seam for the persistence layer
 *       ({@link #DEFAULTS_WIRED_THROUGH_SEAM}). The marker only ever sits on a real seam
 *       ({@link #MARKER_ONLY_ON_SEAMS}), and a default is never component-scanned
 *       ({@link #DEFAULTS_HAVE_NO_STEREOTYPE}) so its {@code @ConditionalOnMissingBean} back-off is reliable.</li>
 * </ul>
 *
 * <p>Two registration models coexist while the migration to {@link ModelCityDisabledIfInherited} rolls out
 * per vertical: verticals still on the {@code @AutoConfiguration} catalog apply {@link #DEFAULTS_HAVE_NO_STEREOTYPE}
 * (R4); migrated verticals apply {@link #DEFAULTS_ARE_SCANNED_AND_DISABLEABLE} (R7) and
 * {@link #DISABLED_MARKER_ONLY_ON_CONCRETE_DEFAULTS} (R8) instead.
 */
public final class ExtensionPointRules {

    private static final String MARKER = ModelCityExtensionPoint.class.getName();

    /** Imports a vertical's production classes (test classes excluded) for the rules to check. */
    public static JavaClasses importVertical(String basePackage) {
        return new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages(basePackage);
    }

    // R1 — no city name in any type (simple name or package) of the platform code.
    private static final ArchCondition<JavaClass> CARRY_NO_CITY_NAME =
            new ArchCondition<>("carry no city name in their type or package name") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                    String fqn = item.getFullName().toLowerCase(Locale.ROOT);
                    for (String token : CityNames.TOKENS) {
                        if (fqn.contains(token)) {
                            events.add(SimpleConditionEvent.violated(item,
                                    item.getFullName() + " contains city token '" + token + "'"));
                            return;
                        }
                    }
                }
            };

    public static final ArchRule NO_CITY_NAMES = classes()
            .should(CARRY_NO_CITY_NAME)
            .as("platform defaults and seams must be city-neutral (no city name in the type)")
            .allowEmptyShould(true);

    // R2 — the marker only ever sits on a real seam: an interface or an abstract class.
    public static final ArchRule MARKER_ONLY_ON_SEAMS = classes()
            .that().areAnnotatedWith(ModelCityExtensionPoint.class)
            .should().beInterfaces()
            .orShould().haveModifier(JavaModifier.ABSTRACT)
            .as("@ModelCityExtensionPoint may only mark a seam (interface or abstract class)")
            .allowEmptyShould(true);

    private static final String NO_REPOSITORY_BEAN = "org.springframework.data.repository.NoRepositoryBean";

    // R3 — every Default* fallback is wired through a @ModelCityExtensionPoint seam. A Default*Repository is
    // a different, Spring-Data-native flavour of the same idea: it binds a @NoRepositoryBean generic
    // repository (the persistence-layer seam recommended in PERSISTENCE-EXTENSIBILITY-ANALYSIS.md) instead of
    // a @ModelCityExtensionPoint interface, so it is accepted through that seam too.
    private static final DescribedPredicate<JavaClass> WIRED_THROUGH_A_SEAM =
            new DescribedPredicate<>("wired through a @ModelCityExtensionPoint seam or a @NoRepositoryBean repository") {
                @Override
                public boolean test(JavaClass item) {
                    boolean viaInterface = item.getAllRawInterfaces().stream()
                            .anyMatch(i -> i.isAnnotatedWith(MARKER));
                    if (viaInterface) {
                        return true;
                    }
                    Optional<JavaClass> superclass = item.getRawSuperclass();
                    while (superclass.isPresent()) {
                        if (superclass.get().isAnnotatedWith(MARKER)) {
                            return true;
                        }
                        superclass = superclass.get().getRawSuperclass();
                    }
                    if (item.getSimpleName().endsWith("Repository")) {
                        return item.getAllRawInterfaces().stream()
                                .anyMatch(i -> i.isAnnotatedWith(NO_REPOSITORY_BEAN));
                    }
                    return false;
                }
            };

    public static final ArchRule DEFAULTS_WIRED_THROUGH_SEAM = classes()
            .that().haveSimpleNameStartingWith("Default")
            .should(beTheCondition(WIRED_THROUGH_A_SEAM))
            .as("every Default* fallback must implement/extend a @ModelCityExtensionPoint seam "
                    + "(a Default*Repository may instead bind a @NoRepositoryBean generic repository)")
            .allowEmptyShould(true);

    // R4 — Default* fallbacks are registered via @AutoConfiguration, never component-scanned.
    public static final ArchRule DEFAULTS_HAVE_NO_STEREOTYPE = noClasses()
            .that().haveSimpleNameStartingWith("Default")
            .should().beAnnotatedWith("org.springframework.stereotype.Service")
            .orShould().beAnnotatedWith("org.springframework.stereotype.Component")
            .orShould().beAnnotatedWith("org.springframework.stereotype.Repository")
            .orShould().beAnnotatedWith("org.springframework.stereotype.Controller")
            .orShould().beAnnotatedWith("org.springframework.web.bind.annotation.RestController")
            .as("Default* fallbacks must not be component-scanned (registered via @AutoConfiguration)")
            .allowEmptyShould(true);

    // R5 — a use-case seam exposes exactly one method (the F0 single-responsibility invariant).
    private static final ArchCondition<JavaClass> EXPOSE_EXACTLY_ONE_METHOD =
            new ArchCondition<>("expose exactly one method") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                    long methods = item.getMethods().stream()
                            .filter(m -> !m.getModifiers().contains(JavaModifier.STATIC))
                            .count();
                    if (methods != 1) {
                        events.add(SimpleConditionEvent.violated(item,
                                item.getFullName() + " exposes " + methods
                                        + " methods (a use-case seam must expose exactly one)"));
                    }
                }
            };

    public static final ArchRule USE_CASE_SEAMS_ARE_SINGLE_METHOD = classes()
            .that().areAnnotatedWith(ModelCityExtensionPoint.class)
            .and().haveSimpleNameEndingWith("UseCase")
            .should(EXPOSE_EXACTLY_ONE_METHOD)
            .as("use-case seams must expose exactly one method")
            .allowEmptyShould(true);

    // --- scanned-defaults model (@ModelCityDisabledIfInherited) ---
    // A migrated vertical no longer ships an @AutoConfiguration catalog: its Default* beans carry a regular
    // stereotype plus @ModelCityDisabledIfInherited and are removed at startup by
    // ModelCityDisabledIfInheritedProcessor when a city bean covers their seam. Migrated verticals apply
    // R7/R8 instead of R4.

    // R7 — every concrete Default* is component-scanned AND disableable. Interfaces (Default*Repository,
    // registered by Spring Data) and abstract classes are exempt.
    private static final ArchCondition<JavaClass> CARRY_A_STEREOTYPE =
            new ArchCondition<>("carry a Spring stereotype so component scanning registers them") {
                private static final String[] STEREOTYPES = {
                        "org.springframework.stereotype.Component",
                        "org.springframework.stereotype.Service",
                        "org.springframework.stereotype.Repository",
                        "org.springframework.stereotype.Controller",
                        "org.springframework.web.bind.annotation.RestController"};

                @Override
                public void check(JavaClass item, ConditionEvents events) {
                    for (String stereotype : STEREOTYPES) {
                        if (item.isAnnotatedWith(stereotype) || item.isMetaAnnotatedWith(stereotype)) {
                            return;
                        }
                    }
                    events.add(SimpleConditionEvent.violated(item,
                            item.getFullName() + " carries no Spring stereotype, so it would never be scanned"));
                }
            };

    public static final ArchRule DEFAULTS_ARE_SCANNED_AND_DISABLEABLE = classes()
            .that().haveSimpleNameStartingWith("Default")
            .and().areNotInterfaces()
            .and().doNotHaveModifier(JavaModifier.ABSTRACT)
            .should().beAnnotatedWith(ModelCityDisabledIfInherited.class)
            .andShould(CARRY_A_STEREOTYPE)
            .as("every concrete Default* must be component-scanned (stereotype) and disableable "
                    + "(@ModelCityDisabledIfInherited) so a city bean covering its seam replaces it")
            .allowEmptyShould(true);

    // R8 — the disable marker only ever sits on a concrete default, never on a seam.
    public static final ArchRule DISABLED_MARKER_ONLY_ON_CONCRETE_DEFAULTS = classes()
            .that().areAnnotatedWith(ModelCityDisabledIfInherited.class)
            .should().notBeInterfaces()
            .andShould().notHaveModifier(JavaModifier.ABSTRACT)
            .as("@ModelCityDisabledIfInherited may only mark a concrete default implementation")
            .allowEmptyShould(true);

    // R6 — a persistence store port (an interface named *Store) is a seam and must carry the marker. Unlike
    // the other rules this is applied selectively (only by verticals whose persistence layer has been turned
    // into a seam), because the conversion rolls out per vertical.
    public static final ArchRule STORE_PORTS_ARE_MARKED = classes()
            .that().areInterfaces()
            .and().haveSimpleNameEndingWith("Store")
            .should().beAnnotatedWith(ModelCityExtensionPoint.class)
            .as("persistence store ports (*Store) must be @ModelCityExtensionPoint seams")
            .allowEmptyShould(true);

    /** Adapts a {@link DescribedPredicate} on a class into a satisfied/violated {@link ArchCondition}. */
    private static ArchCondition<JavaClass> beTheCondition(DescribedPredicate<JavaClass> predicate) {
        return new ArchCondition<>("be " + predicate.getDescription()) {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                if (!predicate.test(item)) {
                    events.add(SimpleConditionEvent.violated(item,
                            item.getFullName() + " is not " + predicate.getDescription()));
                }
            }
        };
    }

    private ExtensionPointRules() {
    }
}
