package com.modelcity.leisure.publicspaces.controller;

import com.modelcity.leisure.publicspaces.controller.model.ReservationDto;
import com.modelcity.leisure.publicspaces.controller.model.ReservationRequestDto;
import com.modelcity.leisure.publicspaces.usecase.CreateReservationUseCase;
import com.modelcity.leisure.publicspaces.usecase.DeleteReservationUseCase;
import com.modelcity.leisure.publicspaces.usecase.GetReservationsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock GetReservationsUseCase<ReservationDto> getReservationsUseCase;
    @Mock CreateReservationUseCase<ReservationDto, ReservationRequestDto> createReservationUseCase;
    @Mock DeleteReservationUseCase deleteReservationUseCase;

    DefaultReservationController controller;

    @BeforeEach
    void setUp() {
        controller = new DefaultReservationController(getReservationsUseCase, createReservationUseCase,
                deleteReservationUseCase);
    }

    @Test
    void getReservations_delegatesToUseCase() {
        LocalDate date = LocalDate.of(2026, 8, 1);
        PageRequest pageable = PageRequest.of(0, 50);
        controller.getReservations(1L, 10L, date, "sub-agent", pageable);
        verify(getReservationsUseCase).execute(1L, 10L, date, "sub-agent", pageable);
    }

    @Test
    void createReservation_delegatesToUseCase() {
        ReservationRequestDto request = new ReservationRequestDto();
        controller.createReservation(1L, 10L, "citizen-sub", request);
        verify(createReservationUseCase).execute(1L, 10L, "citizen-sub", request);
    }

    @Test
    void deleteReservation_delegatesToUseCase() {
        controller.deleteReservation(1L, 10L, 100L, "sub-agent");
        verify(deleteReservationUseCase).execute(1L, 10L, 100L, "sub-agent");
    }
}
