package com.modelcity.leisure.cityroutes.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteDto;

/**
 * Returns the detail of a city route including every locale of each localizable field (admin editing).
 * Not cached.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetCityRouteForEditUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetCityRouteForEditUseCase<T extends CityRouteDto> {

    T execute(Long id, String locale);
}
