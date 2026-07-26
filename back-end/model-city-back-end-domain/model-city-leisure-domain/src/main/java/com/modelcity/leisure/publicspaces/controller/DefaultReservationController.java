package com.modelcity.leisure.publicspaces.controller;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.web.bind.annotation.RestController;
import com.modelcity.leisure.publicspaces.controller.model.ReservationDto;
import com.modelcity.leisure.publicspaces.controller.model.ReservationRequestDto;
import com.modelcity.leisure.publicspaces.usecase.CreateReservationUseCase;
import com.modelcity.leisure.publicspaces.usecase.DeleteReservationUseCase;
import com.modelcity.leisure.publicspaces.usecase.GetReservationsUseCase;

/**
 * Default concrete {@link ReservationController}, bound to the platform DTOs. The component-scanned platform
 * default; disabled at startup when a local deployment provides its own bean for the seam.
 */
@RestController
@ModelCityDisabledIfInherited
public class DefaultReservationController
        extends ReservationController<ReservationDto, ReservationRequestDto> {

    public DefaultReservationController(
            GetReservationsUseCase<ReservationDto> getReservationsUseCase,
            CreateReservationUseCase<ReservationDto, ReservationRequestDto> createReservationUseCase,
            DeleteReservationUseCase deleteReservationUseCase) {
        super(getReservationsUseCase, createReservationUseCase, deleteReservationUseCase);
    }
}
