package com.modelcity.leisure.cityroutes.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Returns the ordered list of places associated with a city route.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetCityRoutePlacesUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetCityRoutePlacesUseCase<T extends CityPlaceSummaryDto> {

    Page<T> execute(Long routeId, Pageable pageable, String locale);
}
