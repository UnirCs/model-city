package com.modelcity.leisure.publicspaces.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceDto;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceRequestDto;

/**
 * Updates name, description, address, coordinates and photo URLs of a public space. Admin only.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultUpdatePublicSpaceUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface UpdatePublicSpaceUseCase<T extends PublicSpaceDto, R extends PublicSpaceRequestDto> {

    T execute(Long id, String sub, R request, String locale);
}
