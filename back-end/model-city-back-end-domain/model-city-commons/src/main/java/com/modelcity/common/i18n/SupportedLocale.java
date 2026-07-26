package com.modelcity.common.i18n;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Single source of truth for the locales Model City serves localized content in. Spanish ({@code es})
 * is both the default and the fallback locale. Codes are ISO 639-1 language tags; matching is by
 * language only (region subtags such as {@code en-US} collapse to {@code en}).
 */
public enum SupportedLocale {

    ES("es"),
    EN("en"),
    FR("fr");

    /** Locale used both as the default when none is requested and as the fallback when a translation is missing. */
    public static final SupportedLocale DEFAULT = ES;

    private final String code;

    SupportedLocale(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public Locale locale() {
        return Locale.forLanguageTag(code);
    }

    /** Resolves a language tag (e.g. {@code "en"}, {@code "en-US"}, {@code "fr_FR"}) to a supported locale, defaulting to {@link #DEFAULT}. */
    public static SupportedLocale from(String code) {
        if (code != null && !code.isBlank()) {
            String language = code.trim().toLowerCase(Locale.ROOT).split("[-_]", 2)[0];
            for (SupportedLocale value : values()) {
                if (value.code.equals(language)) {
                    return value;
                }
            }
        }
        return DEFAULT;
    }

    /** Resolves a {@link Locale} (matching by language) to a supported locale, defaulting to {@link #DEFAULT}. */
    public static SupportedLocale from(Locale locale) {
        return locale == null ? DEFAULT : from(locale.getLanguage());
    }

    /** Supported language codes, e.g. {@code [es, en, fr]}. */
    public static List<String> codes() {
        return Arrays.stream(values()).map(SupportedLocale::code).toList();
    }

    /** Supported locales as {@link Locale} instances, used to configure the {@code LocaleResolver}. */
    public static List<Locale> locales() {
        return Arrays.stream(values()).map(SupportedLocale::locale).toList();
    }
}
