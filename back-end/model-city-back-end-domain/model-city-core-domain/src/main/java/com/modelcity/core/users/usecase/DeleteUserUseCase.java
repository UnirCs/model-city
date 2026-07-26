package com.modelcity.core.users.usecase;


import com.modelcity.common.extensibility.ModelCityExtensionPoint;
/**
 * Permanently removes a citizen record. Restricted to platform admins.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultDeleteUserUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface DeleteUserUseCase {

    void execute(String requestingSub, String targetUserId);
}
