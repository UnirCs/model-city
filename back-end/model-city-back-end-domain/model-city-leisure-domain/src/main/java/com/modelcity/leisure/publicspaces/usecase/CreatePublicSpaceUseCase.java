package com.modelcity.leisure.publicspaces.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceDto;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceRequestDto;

/**
 * Creates a new public space. Admin only.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultCreatePublicSpaceUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface CreatePublicSpaceUseCase<T extends PublicSpaceDto, R extends PublicSpaceRequestDto> {

    T execute(String sub, R request, String locale);
}
