package com.modelcity.mobility.reservations.controller;

import com.modelcity.mobility.reservations.controller.model.StreetReservationDto;
import com.modelcity.mobility.reservations.usecase.GetStreetReservationsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StreetReservationControllerTest {

    @Mock GetStreetReservationsUseCase<StreetReservationDto> getStreetReservationsUseCase;

    DefaultStreetReservationController controller;

    @BeforeEach
    void setUp() {
        controller = new DefaultStreetReservationController(getStreetReservationsUseCase);
    }

    @Test
    void getStreetReservations_delegatesToUseCase() {
        OffsetDateTime from = OffsetDateTime.now().minusDays(1);
        OffsetDateTime to = OffsetDateTime.now();
        controller.getStreetReservations("agent-sub", "1234ABC", from, to, true, 1);
        verify(getStreetReservationsUseCase).execute("1234ABC", from, to, true, 1);
    }
}
