package com.modelcity.core.otp.controller;

import com.modelcity.core.otp.controller.model.CreateChallengeRequestDto;
import com.modelcity.core.otp.controller.model.ValidateChallengeRequestDto;
import com.modelcity.core.otp.usecase.BurnOperationAuthorizationUseCase;
import com.modelcity.core.otp.usecase.CreateChallengeUseCase;
import com.modelcity.core.otp.usecase.GetOperationAuthorizationUseCase;
import com.modelcity.core.otp.usecase.ValidateChallengeUseCase;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.web.bind.annotation.RestController;

/**
 * Default concrete {@link OperationAuthorizationController}. The component-scanned platform default; disabled
 * at startup when a local deployment provides its own bean for the seam.
 */
@RestController
@ModelCityDisabledIfInherited
public class DefaultOperationAuthorizationController extends OperationAuthorizationController<CreateChallengeRequestDto, ValidateChallengeRequestDto> {

    public DefaultOperationAuthorizationController(
            CreateChallengeUseCase<CreateChallengeRequestDto> createChallengeUseCase,
            ValidateChallengeUseCase<ValidateChallengeRequestDto> validateChallengeUseCase,
            GetOperationAuthorizationUseCase getOperationAuthorizationUseCase,
            BurnOperationAuthorizationUseCase burnOperationAuthorizationUseCase) {
        super(createChallengeUseCase, validateChallengeUseCase,
                getOperationAuthorizationUseCase, burnOperationAuthorizationUseCase);
    }
}
