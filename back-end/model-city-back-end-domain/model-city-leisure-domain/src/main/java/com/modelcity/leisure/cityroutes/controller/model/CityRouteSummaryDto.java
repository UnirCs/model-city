package com.modelcity.leisure.cityroutes.controller.model;

import com.modelcity.common.i18n.LocalizedText;
import com.modelcity.leisure.cityroutes.store.model.CityRouteView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Compact representation of a city route for list endpoints, with its name resolved to the requested locale.
 *
 * <p>Extensible DTO (plain class, not a {@code record}) so a local deployment may subclass it and bind the
 * subtype through the generic seams (e.g. {@code GetCityRoutesUseCase<S extends CityRouteSummaryDto>}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CityRouteSummaryDto {

    private Long id;
    private String name;
    private String targetAudience;
    private String imageUrl;
    private Integer estimatedDurationMinutes;
    private int placeCount;

    public static CityRouteSummaryDto from(CityRouteView r, String locale) {
        CityRouteView.Translation t = r.getTranslations().get(locale);
        return new CityRouteSummaryDto(
                r.getId(),
                LocalizedText.resolve(r.getName(), t == null ? null : t.getName()),
                r.getTargetAudience(), r.getImageUrl(), r.getEstimatedDurationMinutes(),
                r.getRoutePlaces().size()
        );
    }
}
