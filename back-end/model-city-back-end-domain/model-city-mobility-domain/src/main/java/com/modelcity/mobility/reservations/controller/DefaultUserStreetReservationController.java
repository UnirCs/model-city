package com.modelcity.mobility.reservations.controller;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.web.bind.annotation.RestController;

import com.modelcity.mobility.reservations.controller.model.StreetReservationDto;
import com.modelcity.mobility.reservations.controller.model.StreetReservationRequestDto;
import com.modelcity.mobility.reservations.usecase.CreateStreetReservationUseCase;
import com.modelcity.mobility.reservations.usecase.GetUserStreetReservationsUseCase;
import com.modelcity.mobility.reservations.usecase.RenewStreetReservationUseCase;

/**
 * Default concrete {@link UserStreetReservationController}. The component-scanned platform default; disabled at startup when a local
 * deployment provides its own bean for the seam.
 */
@RestController
@ModelCityDisabledIfInherited
public class DefaultUserStreetReservationController extends UserStreetReservationController<StreetReservationDto, StreetReservationRequestDto> {

    public DefaultUserStreetReservationController(
            CreateStreetReservationUseCase<StreetReservationDto, StreetReservationRequestDto> createStreetReservationUseCase,
            GetUserStreetReservationsUseCase<StreetReservationDto> getUserStreetReservationsUseCase,
            RenewStreetReservationUseCase<StreetReservationDto, StreetReservationRequestDto> renewStreetReservationUseCase) {
        super(createStreetReservationUseCase, getUserStreetReservationsUseCase, renewStreetReservationUseCase);
    }
}
