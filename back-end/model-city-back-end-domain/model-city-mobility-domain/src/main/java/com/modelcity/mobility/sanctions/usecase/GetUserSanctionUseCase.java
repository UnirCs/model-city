package com.modelcity.mobility.sanctions.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.mobility.sanctions.controller.model.SanctionDto;

/**
 * Returns a single sanction (with image) only if it belongs to a car owned by the caller.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetUserSanctionUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetUserSanctionUseCase<T extends SanctionDto> {

    T execute(String userId, String sub, Long sanctionId);
}
