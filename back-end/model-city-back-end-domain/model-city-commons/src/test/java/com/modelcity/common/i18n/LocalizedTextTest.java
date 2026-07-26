package com.modelcity.common.i18n;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalizedTextTest {

    private record Translation(String value) {
    }

    @Test
    void buildLocaleMap_includesBaseUnderDefaultLocale() {
        Map<String, Translation> translations = Map.of("en", new Translation("Hello"));

        Map<String, String> result = LocalizedText.buildLocaleMap("Hola", translations, Translation::value);

        assertThat(result).containsEntry("es", "Hola");
        assertThat(result).containsEntry("en", "Hello");
    }

    @Test
    void buildLocaleMap_skipsBlankTranslations() {
        Map<String, Translation> translations = Map.of("en", new Translation("  "), "fr", new Translation(""));

        Map<String, String> result = LocalizedText.buildLocaleMap("Hola", translations, Translation::value);

        assertThat(result).containsOnlyKeys("es");
    }

    @Test
    void buildLocaleMap_nullBase_omitsDefaultKey() {
        Map<String, Translation> translations = Map.of("en", new Translation("Hello"));

        Map<String, String> result = LocalizedText.buildLocaleMap(null, translations, Translation::value);

        assertThat(result).containsOnlyKeys("en");
    }

    @Test
    void resolve_returnsTranslatedWhenPresent() {
        assertThat(LocalizedText.resolve("base", "translated")).isEqualTo("translated");
    }

    @Test
    void resolve_returnsBaseWhenTranslatedIsBlank() {
        assertThat(LocalizedText.resolve("base", "  ")).isEqualTo("base");
        assertThat(LocalizedText.resolve("base", null)).isEqualTo("base");
    }

    @Test
    void requireDefault_returnsDefaultLocaleValue() {
        String result = LocalizedText.requireDefault("name", Map.of("es", "Hola", "en", "Hello"));
        assertThat(result).isEqualTo("Hola");
    }

    @Test
    void requireDefault_missingDefaultLocale_throwsBadRequest() {
        assertThatThrownBy(() -> LocalizedText.requireDefault("name", Map.of("en", "Hello")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("name")
                .hasMessageContaining("es");
    }

    @Test
    void requireDefault_nullMap_throwsBadRequest() {
        assertThatThrownBy(() -> LocalizedText.requireDefault("name", null))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void requireDefault_blankDefaultValue_throwsBadRequest() {
        assertThatThrownBy(() -> LocalizedText.requireDefault("name", Map.of("es", "  ")))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void nonDefault_excludesDefaultLocaleAndBlanks() {
        Map<String, String> values = Map.of("es", "Hola", "en", "Hello", "fr", "  ");

        Map<String, String> result = LocalizedText.nonDefault(values);

        assertThat(result).containsOnlyKeys("en");
        assertThat(result).containsEntry("en", "Hello");
    }

    @Test
    void nonDefault_nullMap_returnsEmptyMap() {
        assertThat(LocalizedText.nonDefault(null)).isEmpty();
    }

    @Test
    void nonDefault_unsupportedLocaleCollapsesToDefaultAndIsExcluded() {
        Map<String, String> values = Map.of("es-ES", "Hola de España");

        Map<String, String> result = LocalizedText.nonDefault(values);

        assertThat(result).isEmpty();
    }

    @Test
    void nonDefault_regionVariantCollapsesToSupportedLanguage() {
        Map<String, String> values = Map.of("en-US", "Hello there");

        Map<String, String> result = LocalizedText.nonDefault(values);

        assertThat(result).containsEntry("en", "Hello there");
    }
}
