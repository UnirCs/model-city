package com.modelcity.mobility.reservations.controller;

import com.modelcity.mobility.reservations.controller.model.StreetReservationDto;
import com.modelcity.mobility.reservations.controller.model.StreetReservationRequestDto;
import com.modelcity.mobility.reservations.usecase.CreateStreetReservationUseCase;
import com.modelcity.mobility.reservations.usecase.GetUserStreetReservationsUseCase;
import com.modelcity.mobility.reservations.usecase.RenewStreetReservationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserStreetReservationControllerTest {

    @Mock CreateStreetReservationUseCase<StreetReservationDto, StreetReservationRequestDto> createStreetReservationUseCase;
    @Mock GetUserStreetReservationsUseCase<StreetReservationDto> getUserStreetReservationsUseCase;
    @Mock RenewStreetReservationUseCase<StreetReservationDto, StreetReservationRequestDto> renewStreetReservationUseCase;

    DefaultUserStreetReservationController controller;

    @BeforeEach
    void setUp() {
        controller = new DefaultUserStreetReservationController(createStreetReservationUseCase,
                getUserStreetReservationsUseCase, renewStreetReservationUseCase);
    }

    @Test
    void createReservation_delegatesToUseCase() {
        StreetReservationRequestDto request = new StreetReservationRequestDto();
        controller.createReservation("user-sub", "user-sub", request);
        verify(createStreetReservationUseCase).execute("user-sub", "user-sub", request);
    }

    @Test
    void getReservations_delegatesToUseCase() {
        PageRequest pageable = PageRequest.of(0, 10);
        controller.getReservations("user-sub", "user-sub", pageable);
        verify(getUserStreetReservationsUseCase).execute("user-sub", "user-sub", pageable);
    }

    @Test
    void renewReservation_delegatesToUseCase() {
        StreetReservationRequestDto request = new StreetReservationRequestDto();
        controller.renewReservation("user-sub", 50L, "user-sub", request);
        verify(renewStreetReservationUseCase).execute("user-sub", "user-sub", 50L, request);
    }
}
