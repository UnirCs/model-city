package com.modelcity.leisure.cityplaces.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceDto;
import org.springframework.data.domain.Page;

/**
 * Returns a paginated list of city places, optionally filtered by category.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetCityPlacesUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetCityPlacesUseCase<T extends CityPlaceDto> {

    Page<T> execute(String category, int page, String locale);
}
