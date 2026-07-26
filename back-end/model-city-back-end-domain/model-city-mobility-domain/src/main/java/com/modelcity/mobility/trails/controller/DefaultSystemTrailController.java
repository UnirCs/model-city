package com.modelcity.mobility.trails.controller;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.web.bind.annotation.RestController;

import com.modelcity.mobility.trails.usecase.GetSystemTrailsUseCase;

/**
 * Default concrete {@link SystemTrailController}. The component-scanned platform default; disabled at startup when a local
 * deployment provides its own bean for the seam.
 */
@RestController("mobilityDefaultSystemTrailController")
@ModelCityDisabledIfInherited
public class DefaultSystemTrailController extends SystemTrailController {

    public DefaultSystemTrailController(GetSystemTrailsUseCase getSystemTrailsUseCase) {
        super(getSystemTrailsUseCase);
    }
}
