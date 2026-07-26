package com.modelcity.core.users.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.core.users.controller.model.UserProfileDto;

/**
 * Returns the profile of a citizen. Admins can request any profile; citizens only their own.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetUserUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetUserUseCase<T extends UserProfileDto> {

    T execute(String requestingSub, String targetUserId, String locale);
}
