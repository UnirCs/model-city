package com.modelcity.core.trails.controller;

import com.modelcity.core.trails.usecase.GetSystemTrailsUseCase;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.web.bind.annotation.RestController;

/**
 * Default concrete {@link SystemTrailController}. The component-scanned platform default; disabled at startup
 * when a local deployment provides its own bean for the seam.
 */
@RestController("coreDefaultSystemTrailController")
@ModelCityDisabledIfInherited
public class DefaultSystemTrailController extends SystemTrailController {

    public DefaultSystemTrailController(GetSystemTrailsUseCase getSystemTrailsUseCase) {
        super(getSystemTrailsUseCase);
    }
}
