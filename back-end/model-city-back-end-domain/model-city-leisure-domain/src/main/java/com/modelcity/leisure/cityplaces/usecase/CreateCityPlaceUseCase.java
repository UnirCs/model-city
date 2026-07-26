package com.modelcity.leisure.cityplaces.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceDto;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceRequestDto;

/**
 * Creates a new city place.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultCreateCityPlaceUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface CreateCityPlaceUseCase<T extends CityPlaceDto, R extends CityPlaceRequestDto> {

    T execute(String sub, R request, String locale);
}
