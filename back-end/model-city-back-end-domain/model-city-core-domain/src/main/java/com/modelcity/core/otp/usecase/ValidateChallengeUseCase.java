package com.modelcity.core.otp.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.common.dto.OperationAuthorizationResponseDto;
import com.modelcity.core.otp.controller.model.ValidateChallengeRequestDto;

import java.util.UUID;

/**
 * Validates an OTP challenge, regenerating the code on a wrong attempt until attempts are exhausted.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultValidateChallengeUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface ValidateChallengeUseCase<R extends ValidateChallengeRequestDto> {

    OperationAuthorizationResponseDto execute(UUID id, String sub, R request);
}
