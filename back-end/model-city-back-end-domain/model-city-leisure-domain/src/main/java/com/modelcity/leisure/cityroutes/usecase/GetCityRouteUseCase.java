package com.modelcity.leisure.cityroutes.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteDto;

/**
 * Returns the detail of a single city route.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetCityRouteUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetCityRouteUseCase<T extends CityRouteDto> {

    T execute(Long id, String locale);
}
