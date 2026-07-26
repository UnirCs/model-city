package com.modelcity.leisure.cityplaces.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceDto;

/**
 * Returns the detail of a single city place, resolved to the requested locale.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetCityPlaceUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetCityPlaceUseCase<T extends CityPlaceDto> {

    T execute(Long id, String locale);
}
