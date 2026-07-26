package com.modelcity.leisure.publicspaces.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceDto;

/**
 * Returns the detail of a public space including every locale of each localizable field (admin editing).
 * Not cached.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetPublicSpaceForEditUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetPublicSpaceForEditUseCase<T extends PublicSpaceDto> {

    T execute(Long id, String locale);
}
