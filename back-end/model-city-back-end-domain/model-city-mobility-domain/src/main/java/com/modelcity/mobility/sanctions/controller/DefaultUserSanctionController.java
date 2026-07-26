package com.modelcity.mobility.sanctions.controller;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.web.bind.annotation.RestController;

import com.modelcity.mobility.sanctions.controller.model.SanctionDto;
import com.modelcity.mobility.sanctions.controller.model.SanctionSummaryDto;
import com.modelcity.mobility.sanctions.usecase.GetUserSanctionUseCase;
import com.modelcity.mobility.sanctions.usecase.GetUserSanctionsUseCase;

/**
 * Default concrete {@link UserSanctionController}. The component-scanned platform default; disabled at startup when a local
 * deployment provides its own bean for the seam.
 */
@RestController
@ModelCityDisabledIfInherited
public class DefaultUserSanctionController extends UserSanctionController<SanctionDto, SanctionSummaryDto> {

    public DefaultUserSanctionController(
            GetUserSanctionsUseCase<SanctionSummaryDto> getUserSanctionsUseCase,
            GetUserSanctionUseCase<SanctionDto> getUserSanctionUseCase) {
        super(getUserSanctionsUseCase, getUserSanctionUseCase);
    }
}
