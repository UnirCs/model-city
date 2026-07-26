package com.modelcity.engagement.alerts.controller;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import com.modelcity.engagement.alerts.controller.model.SecurityAlertDto;
import com.modelcity.engagement.alerts.controller.model.SecurityAlertRequestDto;
import com.modelcity.engagement.alerts.usecase.CreateSecurityAlertUseCase;
import com.modelcity.engagement.alerts.usecase.DeleteSecurityAlertUseCase;
import com.modelcity.engagement.alerts.usecase.GetSecurityAlertsUseCase;
import org.springframework.web.bind.annotation.RestController;

/**
 * Default concrete {@link SecurityAlertController}. The component-scanned platform default; disabled at
 * startup when a local deployment provides its own bean for the seam.
 */
@RestController
@ModelCityDisabledIfInherited
public class DefaultSecurityAlertController extends SecurityAlertController<SecurityAlertDto, SecurityAlertRequestDto> {

    public DefaultSecurityAlertController(
            GetSecurityAlertsUseCase<SecurityAlertDto> getSecurityAlertsUseCase,
            CreateSecurityAlertUseCase<SecurityAlertDto, SecurityAlertRequestDto> createSecurityAlertUseCase,
            DeleteSecurityAlertUseCase deleteSecurityAlertUseCase) {
        super(getSecurityAlertsUseCase, createSecurityAlertUseCase, deleteSecurityAlertUseCase);
    }
}
