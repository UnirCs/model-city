package com.modelcity.leisure.cityplaces.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceDto;

/**
 * Returns the detail of a city place including every locale of each localizable field (admin editing).
 * Not cached.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetCityPlaceForEditUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetCityPlaceForEditUseCase<T extends CityPlaceDto> {

    T execute(Long id, String locale);
}
