package com.modelcity.core.otp.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.common.dto.OperationAuthorizationResponseDto;

import java.util.UUID;

/**
 * Marks a VERIFIED authorization as BURNT once the authorized operation has completed.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultBurnOperationAuthorizationUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface BurnOperationAuthorizationUseCase {

    OperationAuthorizationResponseDto execute(UUID id);
}
