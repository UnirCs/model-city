package com.modelcity.mobility.sanctions.controller;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.web.bind.annotation.RestController;

import com.modelcity.mobility.sanctions.controller.model.SanctionDto;
import com.modelcity.mobility.sanctions.controller.model.SanctionRequestDto;
import com.modelcity.mobility.sanctions.controller.model.SanctionSummaryDto;
import com.modelcity.mobility.sanctions.usecase.CreateSanctionUseCase;
import com.modelcity.mobility.sanctions.usecase.GetSanctionUseCase;
import com.modelcity.mobility.sanctions.usecase.GetSanctionsUseCase;

/**
 * Default concrete {@link SanctionController}. The component-scanned platform default; disabled at startup when a local
 * deployment provides its own bean for the seam.
 */
@RestController
@ModelCityDisabledIfInherited
public class DefaultSanctionController extends SanctionController<SanctionDto, SanctionRequestDto, SanctionSummaryDto> {

    public DefaultSanctionController(
            CreateSanctionUseCase<SanctionDto, SanctionRequestDto> createSanctionUseCase,
            GetSanctionsUseCase<SanctionSummaryDto> getSanctionsUseCase,
            GetSanctionUseCase<SanctionDto> getSanctionUseCase) {
        super(createSanctionUseCase, getSanctionsUseCase, getSanctionUseCase);
    }
}
