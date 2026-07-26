package com.modelcity.leisure.cityroutes.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceDto;

/**
 * Returns the detail of a place that belongs to a route.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetCityRoutePlaceUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetCityRoutePlaceUseCase<T extends CityPlaceDto> {

    T execute(Long routeId, Long placeId, String locale);
}
