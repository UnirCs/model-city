package com.modelcity.leisure.cityroutes.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.modelcity.common.i18n.LocalizedText;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceSummaryDto;
import com.modelcity.leisure.cityroutes.store.model.CityRouteView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Full representation of a city route, including its ordered places (summary form), resolved to a locale.
 *
 * <p>Extensible DTO (plain class, not a {@code record}): a local deployment may subclass it to add
 * city-specific fields and bind the subtype through the generic seams
 * (e.g. {@code GetCityRouteUseCase<T extends CityRouteDto>}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CityRouteDto {

    private Long id;
    private String name;
    private String description;
    private String targetAudience;
    private String imageUrl;
    private Integer estimatedDurationMinutes;
    private int placeCount;
    private List<CityPlaceSummaryDto> cityPlaces;
    private Map<String, Map<String, String>> translations;

    public static CityRouteDto from(CityRouteView r, String locale) {
        return build(r, locale, false);
    }

    public static CityRouteDto fromWithTranslations(CityRouteView r, String locale) {
        return build(r, locale, true);
    }

    private static CityRouteDto build(CityRouteView r, String locale, boolean includeTranslations) {
        CityRouteView.Translation t = r.getTranslations().get(locale);
        List<CityPlaceSummaryDto> places = r.getRoutePlaces().stream()
                .map(rp -> CityPlaceSummaryDto.from(rp.getPlace(), locale))
                .toList();
        return new CityRouteDto(
                r.getId(),
                LocalizedText.resolve(r.getName(), t == null ? null : t.getName()),
                LocalizedText.resolve(r.getDescription(), t == null ? null : t.getDescription()),
                r.getTargetAudience(), r.getImageUrl(), r.getEstimatedDurationMinutes(),
                places.size(), places,
                includeTranslations ? allTranslations(r) : null
        );
    }

    private static Map<String, Map<String, String>> allTranslations(CityRouteView r) {
        Map<String, Map<String, String>> all = new LinkedHashMap<>();
        all.put("name", localeMap(r.getName(), CityRouteView.Translation::getName, r));
        all.put("description", localeMap(r.getDescription(), CityRouteView.Translation::getDescription, r));
        return all;
    }

    private static Map<String, String> localeMap(String base,
                                                 Function<CityRouteView.Translation, String> field,
                                                 CityRouteView r) {
        return LocalizedText.buildLocaleMap(base, r.getTranslations(), field);
    }
}
