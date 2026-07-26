package com.modelcity.leisure.cityroutes.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteSummaryDto;
import org.springframework.data.domain.Page;

/**
 * Returns a paginated list of city routes (page size 3).
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetCityRoutesUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetCityRoutesUseCase<S extends CityRouteSummaryDto> {

    Page<S> execute(int page, String locale);
}
