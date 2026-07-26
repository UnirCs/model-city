package com.modelcity.core.users.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.core.users.controller.model.SignInRequestDto;

/**
 * Registers or updates a citizen on sign-in (JIT provisioning).
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultRegisterUserUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface RegisterUserUseCase<R extends SignInRequestDto> {

    void execute(String sub, R request);
}
