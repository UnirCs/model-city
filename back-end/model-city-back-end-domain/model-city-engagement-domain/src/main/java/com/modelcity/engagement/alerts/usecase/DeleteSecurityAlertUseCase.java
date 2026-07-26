package com.modelcity.engagement.alerts.usecase;


import com.modelcity.common.extensibility.ModelCityExtensionPoint;
/**
 * Deletes a security alert by id. Authorization enforced by {@code @ModelCityAccess}.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultDeleteSecurityAlertUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface DeleteSecurityAlertUseCase {

    void execute(Long id, String sub);
}
