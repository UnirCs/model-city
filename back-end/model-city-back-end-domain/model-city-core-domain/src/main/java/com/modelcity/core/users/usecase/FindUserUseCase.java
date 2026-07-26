package com.modelcity.core.users.usecase;


import com.modelcity.common.extensibility.ModelCityExtensionPoint;
/**
 * Checks whether a citizen with a given Auth0 sub exists.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultFindUserUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface FindUserUseCase {

    boolean execute(String sub);
}
