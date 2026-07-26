package com.modelcity.engagement.alerts.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.engagement.alerts.controller.model.SecurityAlertDto;
import com.modelcity.engagement.alerts.controller.model.SecurityAlertRequestDto;

/**
 * Creates a new security alert. Authorization enforced by {@code @ModelCityAccess}.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultCreateSecurityAlertUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface CreateSecurityAlertUseCase<T extends SecurityAlertDto, R extends SecurityAlertRequestDto> {

    T execute(String sub, R request, String locale);
}
