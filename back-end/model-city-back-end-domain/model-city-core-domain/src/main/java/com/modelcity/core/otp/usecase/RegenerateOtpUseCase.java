package com.modelcity.core.otp.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.core.otp.repository.model.OperationAuthorization;

/**
 * Regenerates and sends a new OTP for an existing challenge that failed validation.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultRegenerateOtpUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface RegenerateOtpUseCase {

    void execute(OperationAuthorization auth);
}
