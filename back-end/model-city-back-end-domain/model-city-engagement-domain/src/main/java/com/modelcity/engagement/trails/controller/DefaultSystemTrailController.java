package com.modelcity.engagement.trails.controller;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import com.modelcity.engagement.trails.usecase.GetSystemTrailsUseCase;
import org.springframework.web.bind.annotation.RestController;

/**
 * Default concrete {@link SystemTrailController}. The component-scanned platform default; disabled at startup
 * when a local deployment provides its own bean for the seam.
 */
@RestController("engagementDefaultSystemTrailController")
@ModelCityDisabledIfInherited
public class DefaultSystemTrailController extends SystemTrailController {

    public DefaultSystemTrailController(GetSystemTrailsUseCase getSystemTrailsUseCase) {
        super(getSystemTrailsUseCase);
    }
}
