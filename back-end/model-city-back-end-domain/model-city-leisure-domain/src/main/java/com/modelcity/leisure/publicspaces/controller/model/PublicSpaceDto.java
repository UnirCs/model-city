package com.modelcity.leisure.publicspaces.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.modelcity.common.i18n.LocalizedText;
import com.modelcity.common.util.PhotoUrls;
import com.modelcity.leisure.publicspaces.model.PublicSpaceView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Full detail of a public space (without its resources, fetched separately), resolved to a locale.
 *
 * <p>Extensible DTO (plain class, not a {@code record}): a local deployment may subclass it to add
 * city-specific fields and bind the subtype through the generic seams
 * (e.g. {@code GetPublicSpaceUseCase<T extends PublicSpaceDto>}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublicSpaceDto {

    private Long id;
    private String name;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private List<String> photoUrls;
    private Map<String, Map<String, String>> translations;

    public static PublicSpaceDto from(PublicSpaceView s, String locale) {
        return build(s, locale, false);
    }

    public static PublicSpaceDto fromWithTranslations(PublicSpaceView s, String locale) {
        return build(s, locale, true);
    }

    private static PublicSpaceDto build(PublicSpaceView s, String locale, boolean includeTranslations) {
        PublicSpaceView.Translation t = s.getTranslations().get(locale);
        List<String> photos = PhotoUrls.collect(s.getPhotoUrl1(), s.getPhotoUrl2(), s.getPhotoUrl3());
        return new PublicSpaceDto(
                s.getId(),
                LocalizedText.resolve(s.getName(), t == null ? null : t.getName()),
                LocalizedText.resolve(s.getDescription(), t == null ? null : t.getDescription()),
                LocalizedText.resolve(s.getAddress(), t == null ? null : t.getAddress()),
                s.getLatitude(), s.getLongitude(), photos,
                includeTranslations ? allTranslations(s) : null
        );
    }

    private static Map<String, Map<String, String>> allTranslations(PublicSpaceView s) {
        Map<String, Map<String, String>> all = new LinkedHashMap<>();
        all.put("name", localeMap(s.getName(), PublicSpaceView.Translation::getName, s));
        all.put("description", localeMap(s.getDescription(), PublicSpaceView.Translation::getDescription, s));
        all.put("address", localeMap(s.getAddress(), PublicSpaceView.Translation::getAddress, s));
        return all;
    }

    private static Map<String, String> localeMap(String base,
                                                 Function<PublicSpaceView.Translation, String> field,
                                                 PublicSpaceView s) {
        return LocalizedText.buildLocaleMap(base, s.getTranslations(), field);
    }
}
