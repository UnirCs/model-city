package com.modelcity.core.users.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.core.users.repository.model.UserStatus;

/**
 * Enables or disables a user account. Restricted to platform admins. Admins cannot be disabled.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultSetUserStatusUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface SetUserStatusUseCase {

    void execute(String requestingSub, String targetUserId, UserStatus status);
}
