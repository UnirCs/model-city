package com.modelcity.leisure.cityroutes.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteDto;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteRequestDto;

/**
 * Creates a new city route and its ordered list of places.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultCreateCityRouteUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface CreateCityRouteUseCase<T extends CityRouteDto, R extends CityRouteRequestDto> {

    T execute(String sub, R request, String locale);
}
