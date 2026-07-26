package com.modelcity.leisure.cityroutes.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteDto;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteRequestDto;

/**
 * Fully replaces an existing city route.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultUpdateCityRouteUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface UpdateCityRouteUseCase<T extends CityRouteDto, R extends CityRouteRequestDto> {

    T execute(Long id, String sub, R request, String locale);
}
