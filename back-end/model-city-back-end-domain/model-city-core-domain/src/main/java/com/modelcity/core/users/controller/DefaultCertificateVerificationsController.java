package com.modelcity.core.users.controller;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import com.modelcity.core.users.usecase.HandleCertificateVerificationUseCase;
import org.springframework.web.bind.annotation.RestController;

/**
 * Default concrete {@link CertificateVerificationsController}. The component-scanned platform default; disabled at
 * startup when a local deployment provides its own bean for the seam.
 */
@RestController
@ModelCityDisabledIfInherited
public class DefaultCertificateVerificationsController extends CertificateVerificationsController {

    public DefaultCertificateVerificationsController(HandleCertificateVerificationUseCase certificateVerificationUseCase) {
        super(certificateVerificationUseCase);
    }
}
