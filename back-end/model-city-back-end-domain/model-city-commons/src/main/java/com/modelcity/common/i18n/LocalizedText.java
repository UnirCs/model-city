package com.modelcity.common.i18n;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Helpers for the translation-table localization model, where the base row holds the default-locale
 * ({@code es}) value and a side table holds the remaining locales. Reads fall back to the base value
 * when a translation is missing; writes split a multi-locale payload into the default value and the
 * non-default translations.
 */
public final class LocalizedText {

    private LocalizedText() {
    }

    /**
     * Builds a {@code locale -> value} map from a base (default-locale) string and a translations
     * map. The base value is inserted under {@link SupportedLocale#DEFAULT}; each translation is
     * evaluated with the supplied extractor and included only when non-blank.
     */
    public static <T> Map<String, String> buildLocaleMap(
            String base, Map<String, ? extends T> translations, Function<T, String> extractor) {
        Map<String, String> values = new LinkedHashMap<>();
        if (base != null) {
            values.put(SupportedLocale.DEFAULT.code(), base);
        }
        translations.forEach((code, t) -> {
            String value = extractor.apply(t);
            if (value != null && !value.isBlank()) {
                values.put(code, value);
            }
        });
        return values;
    }

    /** Returns {@code translated} when present and non-blank, otherwise the base (default-locale) value. */
    public static String resolve(String base, String translated) {
        return (translated != null && !translated.isBlank()) ? translated : base;
    }

    /**
     * Validates that a multi-locale write payload carries the required default ({@code es}) value and
     * returns it. The default value is stored in the base column; raises a {@code 400 Bad Request}
     * (via {@link ResponseStatusException}) when missing, surfaced by the global exception handler.
     */
    public static String requireDefault(String field, Map<String, String> values) {
        String defaultValue = values == null ? null : values.get(SupportedLocale.DEFAULT.code());
        if (defaultValue == null || defaultValue.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Field '" + field + "' requires a value for the default locale '"
                            + SupportedLocale.DEFAULT.code() + "'");
        }
        return defaultValue;
    }

    /**
     * Returns the supported non-default translations (e.g. {@code en}, {@code fr}) from a write payload,
     * keyed by language code, dropping blanks and unsupported/default locales. These are the rows to
     * persist in the translation table.
     */
    public static Map<String, String> nonDefault(Map<String, String> values) {
        Map<String, String> result = new LinkedHashMap<>();
        if (values == null) {
            return result;
        }
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String code = SupportedLocale.from(entry.getKey()).code();
            String value = entry.getValue();
            if (!code.equals(SupportedLocale.DEFAULT.code()) && value != null && !value.isBlank()) {
                result.put(code, value);
            }
        }
        return result;
    }
}
