package com.modelcity.leisure.cityplaces.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceDto;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceRequestDto;

/**
 * Fully replaces an existing city place.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultUpdateCityPlaceUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface UpdateCityPlaceUseCase<T extends CityPlaceDto, R extends CityPlaceRequestDto> {

    T execute(Long id, String sub, R request, String locale);
}
