package com.modelcity.leisure.events.controller;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.web.bind.annotation.RestController;
import com.modelcity.leisure.events.controller.model.CitizenTicketDto;
import com.modelcity.leisure.events.usecase.GetCitizenTicketsUseCase;

/**
 * Default concrete {@link CitizenTicketController}, bound to the platform DTO. The component-scanned platform
 * default; disabled at startup when a local deployment provides its own bean for the seam.
 */
@RestController
@ModelCityDisabledIfInherited
public class DefaultCitizenTicketController extends CitizenTicketController<CitizenTicketDto> {

    public DefaultCitizenTicketController(GetCitizenTicketsUseCase<CitizenTicketDto> getCitizenTicketsUseCase) {
        super(getCitizenTicketsUseCase);
    }
}
