package com.modelcity.leisure.cityroutes.controller.model;

import com.modelcity.common.i18n.LocalizedText;
import com.modelcity.leisure.cityroutes.store.model.CityRouteView;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Request body for creating or replacing a city route. The localizable {@code name} and
 * {@code description} are multi-locale maps ({@code locale -> text}) with a mandatory {@code es} entry.
 *
 * <p>Extensible DTO (plain class, not a {@code record}): a city may subclass it and bind the subtype through
 * the generic seams (e.g. {@code CreateCityRouteUseCase<T extends CityRouteDto, R extends CityRouteRequestDto>}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CityRouteRequestDto {

    @NotEmpty
    private Map<String, String> name;
    @NotEmpty
    private Map<String, String> description;
    @NotBlank
    private String targetAudience;
    private String imageUrl;
    private Integer estimatedDurationMinutes;
    @NotEmpty
    private List<@NotNull Long> cityPlaceIds;

    public static CityRouteRequestDto fromEntity(CityRouteView r) {
        List<Long> ids = r.getRoutePlaces().stream()
                .map(rp -> rp.getPlace().getId())
                .toList();
        return new CityRouteRequestDto(
                LocalizedText.buildLocaleMap(r.getName(), r.getTranslations(), CityRouteView.Translation::getName),
                LocalizedText.buildLocaleMap(r.getDescription(), r.getTranslations(), CityRouteView.Translation::getDescription),
                r.getTargetAudience(), r.getImageUrl(), r.getEstimatedDurationMinutes(), ids
        );
    }

}
