package com.modelcity.leisure.publicspaces.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceSummaryDto;
import org.springframework.data.domain.Page;

/**
 * Returns the paginated list of active public spaces (summary view).
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetPublicSpacesUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetPublicSpacesUseCase<S extends PublicSpaceSummaryDto> {

    Page<S> execute(int page, String locale);
}
