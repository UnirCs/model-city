package com.modelcity.leisure.publicspaces.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceDto;

/**
 * Returns the detail of a single active public space.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetPublicSpaceUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetPublicSpaceUseCase<T extends PublicSpaceDto> {

    T execute(Long id, String locale);
}
