package com.modelcity.leisure.cityplaces.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.modelcity.common.i18n.LocalizedText;
import com.modelcity.common.util.PhotoUrls;
import com.modelcity.leisure.cityplaces.store.model.CityPlaceView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Full representation of a city place, with its localizable fields resolved to the requested locale.
 *
 * <p>Extensible DTO (plain class, not a {@code record}): a local deployment may subclass it to add
 * city-specific fields and have its overridden use cases / controllers work with the subtype through the
 * generic seams (e.g. {@code GetCityPlaceUseCase<T extends CityPlaceDto>}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CityPlaceDto {

    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
    private String description;
    private String address;
    private List<String> photoUrls;
    private String accessInfo;
    private String accessibilityInfo;
    private String category;
    private Integer visitDurationMinutes;
    private Map<String, Map<String, String>> translations;

    /** Resolves the place to {@code locale} (falling back to the default locale per field). */
    public static CityPlaceDto from(CityPlaceView p, String locale) {
        return build(p, locale, false);
    }

    /** As {@link #from} but also includes every locale of each localizable field (admin editing). */
    public static CityPlaceDto fromWithTranslations(CityPlaceView p, String locale) {
        return build(p, locale, true);
    }

    private static CityPlaceDto build(CityPlaceView p, String locale, boolean includeTranslations) {
        CityPlaceView.Translation t = p.getTranslations().get(locale);
        List<String> photos = PhotoUrls.collect(p.getPhotoUrl1(), p.getPhotoUrl2(), p.getPhotoUrl3());
        return new CityPlaceDto(
                p.getId(),
                LocalizedText.resolve(p.getName(), t == null ? null : t.getName()),
                p.getLatitude(), p.getLongitude(),
                LocalizedText.resolve(p.getDescription(), t == null ? null : t.getDescription()),
                LocalizedText.resolve(p.getAddress(), t == null ? null : t.getAddress()),
                photos,
                LocalizedText.resolve(p.getAccessInfo(), t == null ? null : t.getAccessInfo()),
                LocalizedText.resolve(p.getAccessibilityInfo(), t == null ? null : t.getAccessibilityInfo()),
                p.getCategory(), p.getVisitDurationMinutes(),
                includeTranslations ? allTranslations(p) : null
        );
    }

    /** Builds {@code field -> {locale -> value}} maps including the default-locale base values. */
    private static Map<String, Map<String, String>> allTranslations(CityPlaceView p) {
        Map<String, Map<String, String>> all = new LinkedHashMap<>();
        all.put("name", localeMap(p.getName(), CityPlaceView.Translation::getName, p));
        all.put("description", localeMap(p.getDescription(), CityPlaceView.Translation::getDescription, p));
        all.put("address", localeMap(p.getAddress(), CityPlaceView.Translation::getAddress, p));
        all.put("accessInfo", localeMap(p.getAccessInfo(), CityPlaceView.Translation::getAccessInfo, p));
        all.put("accessibilityInfo", localeMap(p.getAccessibilityInfo(), CityPlaceView.Translation::getAccessibilityInfo, p));
        return all;
    }

    private static Map<String, String> localeMap(String base,
                                                  Function<CityPlaceView.Translation, String> field,
                                                  CityPlaceView p) {
        return LocalizedText.buildLocaleMap(base, p.getTranslations(), field);
    }
}
