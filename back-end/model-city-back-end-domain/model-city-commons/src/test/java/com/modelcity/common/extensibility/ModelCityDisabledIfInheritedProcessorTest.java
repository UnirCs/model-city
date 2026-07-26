package com.modelcity.common.extensibility;

import com.modelcity.common.config.ModelCityExtensibilityAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the {@link ModelCityDisabledIfInherited} contract in isolation: a component-scanned platform
 * default stays active while nobody covers its seam, and backs off through any of the override doors — a
 * subclass of the default, a direct implementation of the {@link ModelCityExtensionPoint} seam, or a
 * {@code @Bean} method declared with the seam type.
 */
class ModelCityDisabledIfInheritedProcessorTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ModelCityExtensibilityAutoConfiguration.class))
            .withBean("defaultGreeting", DefaultGreeting.class);

    @Test
    void defaultStaysActiveWithoutOverride() {
        runner.run(context -> {
            assertThat(context).hasBean("defaultGreeting");
            assertThat(context).hasSingleBean(GreetingPort.class);
            assertThat(context.getBean(GreetingPort.class)).isInstanceOf(DefaultGreeting.class);
        });
    }

    @Test
    void subclassOfDefaultDisablesIt() {
        runner.withBean("cityGreeting", SubclassOverride.class).run(context -> {
            assertThat(context).doesNotHaveBean("defaultGreeting");
            assertThat(context).hasSingleBean(GreetingPort.class);
            assertThat(context.getBean(GreetingPort.class)).isInstanceOf(SubclassOverride.class);
        });
    }

    @Test
    void directSeamImplementationDisablesDefault() {
        runner.withBean("cityGreeting", InterfaceOverride.class).run(context -> {
            assertThat(context).doesNotHaveBean("defaultGreeting");
            assertThat(context).hasSingleBean(GreetingPort.class);
            assertThat(context.getBean(GreetingPort.class)).isInstanceOf(InterfaceOverride.class);
        });
    }

    @Test
    void beanMethodDeclaredAsSeamTypeDisablesDefault() {
        runner.withUserConfiguration(SeamTypedOverrideConfig.class).run(context -> {
            assertThat(context).doesNotHaveBean("defaultGreeting");
            assertThat(context).hasSingleBean(GreetingPort.class);
            assertThat(context.getBean(GreetingPort.class)).isInstanceOf(InterfaceOverride.class);
        });
    }

    @Test
    void defaultWithoutSeamFallsBackToItsOwnClass() {
        ApplicationContextRunner standalone = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ModelCityExtensibilityAutoConfiguration.class))
                .withBean("defaultStandalone", DefaultStandalone.class);
        standalone.run(context -> assertThat(context).hasBean("defaultStandalone"));
        standalone.withBean("cityStandalone", StandaloneOverride.class).run(context -> {
            assertThat(context).doesNotHaveBean("defaultStandalone");
            assertThat(context).hasBean("cityStandalone");
        });
    }

    @Test
    void unrelatedSeamsDoNotInterfere() {
        runner.withBean("defaultOther", DefaultOther.class)
                .withBean("cityGreeting", SubclassOverride.class)
                .run(context -> {
                    assertThat(context).doesNotHaveBean("defaultGreeting");
                    assertThat(context).hasBean("defaultOther");
                    assertThat(context.getBean(OtherPort.class)).isInstanceOf(DefaultOther.class);
                });
    }

    @Test
    void chainOfAnnotatedDefaultsResolvesToTheMostDerivedBean() {
        runner.withBean("annotatedMiddle", AnnotatedMiddle.class)
                .withBean("leafOverride", LeafOverride.class)
                .run(context -> {
                    assertThat(context).doesNotHaveBean("defaultGreeting");
                    assertThat(context).doesNotHaveBean("annotatedMiddle");
                    assertThat(context).hasSingleBean(GreetingPort.class);
                    assertThat(context.getBean(GreetingPort.class)).isInstanceOf(LeafOverride.class);
                });
    }

    // --- fixtures ---

    @ModelCityExtensionPoint
    interface GreetingPort {
        String greet();
    }

    @ModelCityDisabledIfInherited
    static class DefaultGreeting implements GreetingPort {
        @Override
        public String greet() {
            return "default";
        }
    }

    static class SubclassOverride extends DefaultGreeting {
        @Override
        public String greet() {
            return "city-subclass";
        }
    }

    static class InterfaceOverride implements GreetingPort {
        @Override
        public String greet() {
            return "city-interface";
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class SeamTypedOverrideConfig {
        @Bean
        GreetingPort cityGreeting() {
            return new InterfaceOverride();
        }
    }

    @ModelCityDisabledIfInherited
    static class DefaultStandalone {
    }

    static class StandaloneOverride extends DefaultStandalone {
    }

    @ModelCityExtensionPoint
    interface OtherPort {
    }

    @ModelCityDisabledIfInherited
    static class DefaultOther implements OtherPort {
    }

    @ModelCityDisabledIfInherited
    static class AnnotatedMiddle extends DefaultGreeting {
        @Override
        public String greet() {
            return "middle";
        }
    }

    static class LeafOverride extends AnnotatedMiddle {
        @Override
        public String greet() {
            return "leaf";
        }
    }
}
