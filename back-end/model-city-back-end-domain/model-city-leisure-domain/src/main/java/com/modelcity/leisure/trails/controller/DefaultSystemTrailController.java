package com.modelcity.leisure.trails.controller;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.web.bind.annotation.RestController;
import com.modelcity.leisure.trails.usecase.GetSystemTrailsUseCase;

/**
 * Default concrete {@link SystemTrailController}. The component-scanned platform default; disabled at startup
 * when a local deployment provides its own bean for the seam.
 */
@RestController("leisureDefaultSystemTrailController")
@ModelCityDisabledIfInherited
public class DefaultSystemTrailController extends SystemTrailController {

    public DefaultSystemTrailController(GetSystemTrailsUseCase getSystemTrailsUseCase) {
        super(getSystemTrailsUseCase);
    }
}
