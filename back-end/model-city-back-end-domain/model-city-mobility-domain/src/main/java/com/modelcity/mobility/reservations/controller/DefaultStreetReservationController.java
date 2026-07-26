package com.modelcity.mobility.reservations.controller;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.web.bind.annotation.RestController;

import com.modelcity.mobility.reservations.controller.model.StreetReservationDto;
import com.modelcity.mobility.reservations.usecase.GetStreetReservationsUseCase;

/**
 * Default concrete {@link StreetReservationController}. The component-scanned platform default; disabled at startup when a local
 * deployment provides its own bean for the seam.
 */
@RestController
@ModelCityDisabledIfInherited
public class DefaultStreetReservationController extends StreetReservationController<StreetReservationDto> {

    public DefaultStreetReservationController(GetStreetReservationsUseCase<StreetReservationDto> getStreetReservationsUseCase) {
        super(getStreetReservationsUseCase);
    }
}
