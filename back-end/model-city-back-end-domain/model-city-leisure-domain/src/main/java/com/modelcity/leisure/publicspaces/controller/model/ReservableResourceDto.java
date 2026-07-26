package com.modelcity.leisure.publicspaces.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.modelcity.common.i18n.LocalizedText;
import com.modelcity.leisure.publicspaces.model.ReservableResourceView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Public-facing representation of a reservable resource, resolved to the requested locale.
 *
 * <p>Extensible DTO (plain class, not a {@code record}): a local deployment may subclass it and bind the
 * subtype through the generic seams (e.g. {@code GetReservableResourcesUseCase<T extends ReservableResourceDto>}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReservableResourceDto {

    private Long id;
    private Long publicSpaceId;
    private String name;
    private String description;
    private String resourceType;
    private Map<String, Map<String, String>> translations;

    public static ReservableResourceDto from(ReservableResourceView r, String locale) {
        return build(r, locale, false);
    }

    public static ReservableResourceDto fromWithTranslations(ReservableResourceView r, String locale) {
        return build(r, locale, true);
    }

    private static ReservableResourceDto build(ReservableResourceView r, String locale, boolean includeTranslations) {
        ReservableResourceView.Translation t = r.getTranslations().get(locale);
        return new ReservableResourceDto(
                r.getId(), r.getPublicSpaceId(),
                LocalizedText.resolve(r.getName(), t == null ? null : t.getName()),
                LocalizedText.resolve(r.getDescription(), t == null ? null : t.getDescription()),
                r.getResourceType(),
                includeTranslations ? allTranslations(r) : null
        );
    }

    private static Map<String, Map<String, String>> allTranslations(ReservableResourceView r) {
        Map<String, Map<String, String>> all = new LinkedHashMap<>();
        all.put("name", localeMap(r.getName(), ReservableResourceView.Translation::getName, r));
        all.put("description", localeMap(r.getDescription(), ReservableResourceView.Translation::getDescription, r));
        return all;
    }

    private static Map<String, String> localeMap(String base,
                                                 Function<ReservableResourceView.Translation, String> field,
                                                 ReservableResourceView r) {
        return LocalizedText.buildLocaleMap(base, r.getTranslations(), field);
    }
}
