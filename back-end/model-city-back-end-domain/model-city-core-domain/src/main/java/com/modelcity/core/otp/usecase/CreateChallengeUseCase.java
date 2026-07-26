package com.modelcity.core.otp.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.common.dto.OperationAuthorizationResponseDto;
import com.modelcity.core.otp.controller.model.CreateChallengeRequestDto;

/**
 * Creates an OTP challenge (operation authorization) and emails the code.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultCreateChallengeUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface CreateChallengeUseCase<R extends CreateChallengeRequestDto> {

    OperationAuthorizationResponseDto execute(String sub, R request);
}
