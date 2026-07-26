package com.modelcity.common.i18n;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class SupportedLocaleTest {

    @Test
    void from_string_exactMatch() {
        assertThat(SupportedLocale.from("en")).isEqualTo(SupportedLocale.EN);
        assertThat(SupportedLocale.from("fr")).isEqualTo(SupportedLocale.FR);
    }

    @Test
    void from_string_caseInsensitive() {
        assertThat(SupportedLocale.from("EN")).isEqualTo(SupportedLocale.EN);
    }

    @Test
    void from_string_regionVariantCollapsesToLanguage() {
        assertThat(SupportedLocale.from("en-US")).isEqualTo(SupportedLocale.EN);
        assertThat(SupportedLocale.from("fr_FR")).isEqualTo(SupportedLocale.FR);
    }

    @Test
    void from_string_unsupportedOrNull_fallsBackToDefault() {
        assertThat(SupportedLocale.from("de")).isEqualTo(SupportedLocale.DEFAULT);
        assertThat(SupportedLocale.from((String) null)).isEqualTo(SupportedLocale.DEFAULT);
        assertThat(SupportedLocale.from("")).isEqualTo(SupportedLocale.DEFAULT);
        assertThat(SupportedLocale.from("   ")).isEqualTo(SupportedLocale.DEFAULT);
    }

    @Test
    void from_locale_matchesByLanguage() {
        assertThat(SupportedLocale.from(Locale.forLanguageTag("en-GB"))).isEqualTo(SupportedLocale.EN);
    }

    @Test
    void from_nullLocale_fallsBackToDefault() {
        assertThat(SupportedLocale.from((Locale) null)).isEqualTo(SupportedLocale.DEFAULT);
    }

    @Test
    void codes_returnsAllSupportedCodes() {
        assertThat(SupportedLocale.codes()).containsExactly("es", "en", "fr");
    }

    @Test
    void locales_returnsAllSupportedLocales() {
        assertThat(SupportedLocale.locales()).extracting(Locale::getLanguage)
                .containsExactly("es", "en", "fr");
    }

    @Test
    void defaultLocale_isSpanish() {
        assertThat(SupportedLocale.DEFAULT).isEqualTo(SupportedLocale.ES);
    }
}
