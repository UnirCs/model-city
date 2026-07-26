package com.modelcity.leisure.cityplaces.controller.model;

import com.modelcity.common.i18n.LocalizedText;
import com.modelcity.leisure.cityplaces.store.model.CityPlaceView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Compact representation of a city place, with its name resolved to the requested locale.
 *
 * <p>Extensible DTO (plain class, not a {@code record}) so a local deployment may subclass it and bind
 * the subtype through the generic seams (e.g. {@code GetCityRoutePlacesUseCase<T extends CityPlaceSummaryDto>}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CityPlaceSummaryDto {

    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
    private String category;
    private String coverPhotoUrl;

    public static CityPlaceSummaryDto from(CityPlaceView p, String locale) {
        CityPlaceView.Translation t = p.getTranslations().get(locale);
        return new CityPlaceSummaryDto(
                p.getId(),
                LocalizedText.resolve(p.getName(), t == null ? null : t.getName()),
                p.getLatitude(), p.getLongitude(),
                p.getCategory(), p.getPhotoUrl1()
        );
    }
}
