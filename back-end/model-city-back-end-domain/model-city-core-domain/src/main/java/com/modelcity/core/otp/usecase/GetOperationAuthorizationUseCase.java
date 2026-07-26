package com.modelcity.core.otp.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.common.dto.OperationAuthorizationResponseDto;

import java.util.UUID;

/**
 * Returns an operation authorization by id.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetOperationAuthorizationUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetOperationAuthorizationUseCase {

    OperationAuthorizationResponseDto execute(UUID id);
}
