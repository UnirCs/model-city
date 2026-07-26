package com.modelcity.leisure.cityplaces.controller.model;

import com.modelcity.common.i18n.LocalizedText;
import com.modelcity.common.util.PhotoUrls;
import com.modelcity.leisure.cityplaces.store.model.CityPlaceView;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Request body for creating or replacing a city place. Localizable fields are multi-locale maps
 * ({@code locale -> text}); the {@code es} entry is mandatory (it is the default/fallback value).
 *
 * <p>Extensible DTO (plain class, not a {@code record}): a city may subclass it to accept extra input
 * fields and bind the subtype through the generic seams
 * (e.g. {@code CreateCityPlaceUseCase<T extends CityPlaceDto, R extends CityPlaceRequestDto>}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CityPlaceRequestDto {

    @NotEmpty
    private Map<String, String> name;
    @NotNull
    private Double latitude;
    @NotNull
    private Double longitude;
    @NotEmpty
    private Map<String, String> description;
    private Map<String, String> address;
    @Size(max = 3)
    private List<String> photoUrls;
    private Map<String, String> accessInfo;
    private Map<String, String> accessibilityInfo;
    private String category;
    private Integer visitDurationMinutes;

    public static CityPlaceRequestDto fromEntity(CityPlaceView p) {
        List<String> photos = PhotoUrls.collect(p.getPhotoUrl1(), p.getPhotoUrl2(), p.getPhotoUrl3());
        return new CityPlaceRequestDto(
                LocalizedText.buildLocaleMap(p.getName(), p.getTranslations(), CityPlaceView.Translation::getName),
                p.getLatitude(), p.getLongitude(),
                LocalizedText.buildLocaleMap(p.getDescription(), p.getTranslations(), CityPlaceView.Translation::getDescription),
                LocalizedText.buildLocaleMap(p.getAddress(), p.getTranslations(), CityPlaceView.Translation::getAddress),
                photos,
                LocalizedText.buildLocaleMap(p.getAccessInfo(), p.getTranslations(), CityPlaceView.Translation::getAccessInfo),
                LocalizedText.buildLocaleMap(p.getAccessibilityInfo(), p.getTranslations(), CityPlaceView.Translation::getAccessibilityInfo),
                p.getCategory(), p.getVisitDurationMinutes()
        );
    }

}
