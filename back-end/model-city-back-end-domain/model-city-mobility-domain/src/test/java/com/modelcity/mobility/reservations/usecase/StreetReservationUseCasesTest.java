package com.modelcity.mobility.reservations.usecase;

import com.modelcity.mobility.cars.controller.model.CarRequestDto;
import com.modelcity.mobility.cars.store.CarStore;
import com.modelcity.mobility.cars.store.model.CarView;
import com.modelcity.mobility.reservations.controller.model.StreetReservationDto;
import com.modelcity.mobility.reservations.controller.model.StreetReservationRequestDto;
import com.modelcity.mobility.reservations.repository.model.ReservationStatus;
import com.modelcity.mobility.reservations.store.StreetReservationStore;
import com.modelcity.mobility.reservations.store.model.StreetReservationView;
import com.modelcity.mobility.trails.SystemTrailGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StreetReservationUseCasesTest {

    @Mock
    @SuppressWarnings("unchecked")
    StreetReservationStore<StreetReservationView> streetReservationStore;

    @Mock
    @SuppressWarnings("unchecked")
    CarStore<CarView, CarRequestDto> carStore;

    @Mock
    SystemTrailGenerator systemTrailGenerator;

    private CarView mockCarView(Long id, String ownerSub, String plate) {
        CarView car = mock(CarView.class);
        when(car.getId()).thenReturn(id);
        when(car.getOwnerSub()).thenReturn(ownerSub);
        when(car.getLicensePlate()).thenReturn(plate);
        when(car.getNickname()).thenReturn("Mi Coche");
        return car;
    }

    private StreetReservationView mockReservationView(Long id, String userSub, Long carId) {
        StreetReservationView view = mock(StreetReservationView.class);
        CarView car = mockCarView(carId, userSub, "1234ABC");
        when(view.getId()).thenReturn(id);
        when(view.getUserSub()).thenReturn(userSub);
        when(view.getCar()).thenReturn(car);
        when(view.getLatitude()).thenReturn(40.416775);
        when(view.getLongitude()).thenReturn(-3.703790);
        when(view.getCreatedAt()).thenReturn(OffsetDateTime.now());
        when(view.getExpiresAt()).thenReturn(OffsetDateTime.now().plusHours(2));
        when(view.getStatus()).thenReturn(ReservationStatus.PENDING);
        when(view.getPricePaid()).thenReturn(BigDecimal.TEN);
        when(view.getCurrency()).thenReturn("EUR");
        return view;
    }

    private StreetReservationRequestDto buildRequest(Long carId, int durationMinutes) {
        StreetReservationRequestDto req = new StreetReservationRequestDto();
        req.setCarId(carId);
        req.setLatitude(40.416775);
        req.setLongitude(-3.703790);
        req.setDurationMinutes(durationMinutes);
        req.setCheckoutSessionId("session-123");
        req.setPrice(BigDecimal.TEN);
        return req;
    }

    @Nested
    class CreateStreetReservationTests {

        DefaultCreateStreetReservationUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultCreateStreetReservationUseCase(carStore, streetReservationStore, systemTrailGenerator);
        }

        @Test
        void execute_createsReservationForOwner() {
            CarView car = mockCarView(10L, "user-sub", "1234ABC");
            StreetReservationView saved = mockReservationView(100L, "user-sub", 10L);
            StreetReservationRequestDto request = buildRequest(10L, 60);

            when(carStore.findById(10L)).thenReturn(Optional.of(car));
            when(streetReservationStore.create(eq("user-sub"), eq(10L), any(), any(), any(), any(), isNull(), eq("session-123"), any()))
                    .thenReturn(saved);

            StreetReservationDto result = useCase.execute("user-sub", "user-sub", request);

            assertThat(result.getId()).isEqualTo(100L);
            verify(systemTrailGenerator).streetReservationCreated(saved);
        }

        @Test
        void execute_throwsForbiddenWhenCallerMismatch() {
            assertThatThrownBy(() -> useCase.execute("other-user", "caller-sub", buildRequest(10L, 60)))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Caller does not match");
        }

        @Test
        void execute_throwsNotFoundWhenCarNotFound() {
            when(carStore.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute("user-sub", "user-sub", buildRequest(99L, 60)))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Car not found");
        }

        @Test
        void execute_throwsForbiddenWhenCarBelongsToOtherUser() {
            CarView car = mockCarView(10L, "another-user", "1234ABC");
            when(carStore.findById(10L)).thenReturn(Optional.of(car));

            assertThatThrownBy(() -> useCase.execute("user-sub", "user-sub", buildRequest(10L, 60)))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Car does not belong");
        }
    }

    @Nested
    class GetStreetReservationsTests {

        DefaultGetStreetReservationsUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetStreetReservationsUseCase(streetReservationStore);
        }

        @Test
        @SuppressWarnings("unchecked")
        void execute_returnsPageOfReservations() {
            StreetReservationView view = mockReservationView(1L, "user-sub", 10L);
            Page<StreetReservationView> page = new PageImpl<>(List.of(view));
            doReturn(page).when(streetReservationStore).search(any(), any(), any(), any(), any(Pageable.class));

            Page<StreetReservationDto> result = useCase.execute(null, null, null, null, 0);

            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @SuppressWarnings("unchecked")
        void execute_withFilters_passesThemToStore() {
            Page<StreetReservationView> emptyPage = new PageImpl<>(List.of());
            doReturn(emptyPage).when(streetReservationStore).search(eq("1234ABC"), any(), any(), eq(true), any(Pageable.class));

            Page<StreetReservationDto> result = useCase.execute("1234ABC", null, null, true, 0);

            assertThat(result.getTotalElements()).isZero();
            verify(streetReservationStore).search(eq("1234ABC"), any(), any(), eq(true), any(Pageable.class));
        }
    }

    @Nested
    class GetUserStreetReservationsTests {

        DefaultGetUserStreetReservationsUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetUserStreetReservationsUseCase(streetReservationStore);
        }

        @Test
        @SuppressWarnings("unchecked")
        void execute_returnsUserHistory() {
            StreetReservationView view = mockReservationView(1L, "user-sub", 10L);
            Page<StreetReservationView> page = new PageImpl<>(List.of(view));
            doReturn(page).when(streetReservationStore).findUserHistory(eq("user-sub"), any(OffsetDateTime.class), any(Pageable.class));

            Page<StreetReservationDto> result = useCase.execute("user-sub", "user-sub", PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        void execute_throwsForbiddenWhenCallerMismatch() {
            assertThatThrownBy(() -> useCase.execute("other-user", "caller-sub", PageRequest.of(0, 10)))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Caller does not match");
        }
    }

    @Nested
    class RenewStreetReservationTests {

        DefaultRenewStreetReservationUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultRenewStreetReservationUseCase(streetReservationStore, carStore, systemTrailGenerator);
        }

        @Test
        void execute_renewsActiveReservation() {
            StreetReservationView original = mockReservationView(50L, "user-sub", 10L);
            when(original.getExpiresAt()).thenReturn(OffsetDateTime.now().plusHours(1));
            CarView car = mockCarView(10L, "user-sub", "1234ABC");
            StreetReservationView renewed = mockReservationView(51L, "user-sub", 10L);
            StreetReservationRequestDto request = buildRequest(10L, 60);

            when(streetReservationStore.findById(50L)).thenReturn(Optional.of(original));
            when(carStore.findById(10L)).thenReturn(Optional.of(car));
            when(streetReservationStore.create(eq("user-sub"), eq(10L), any(), any(), any(), any(), eq(50L), any(), any()))
                    .thenReturn(renewed);

            StreetReservationDto result = useCase.execute("user-sub", "user-sub", 50L, request);

            assertThat(result.getId()).isEqualTo(51L);
            verify(systemTrailGenerator).streetReservationRenewed(renewed);
        }

        @Test
        void execute_throwsForbiddenWhenCallerMismatch() {
            assertThatThrownBy(() -> useCase.execute("other-user", "caller-sub", 50L, buildRequest(10L, 60)))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Caller does not match");
        }

        @Test
        void execute_throwsNotFoundWhenReservationMissing() {
            when(streetReservationStore.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute("user-sub", "user-sub", 99L, buildRequest(10L, 60)))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Reservation not found");
        }

        @Test
        void execute_throwsForbiddenWhenReservationBelongsToOtherUser() {
            StreetReservationView original = mockReservationView(50L, "another-user", 10L);
            when(original.getExpiresAt()).thenReturn(OffsetDateTime.now().plusHours(1));
            when(streetReservationStore.findById(50L)).thenReturn(Optional.of(original));

            assertThatThrownBy(() -> useCase.execute("user-sub", "user-sub", 50L, buildRequest(10L, 60)))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Reservation belongs");
        }

        @Test
        void execute_throwsWhenReservationExpired() {
            StreetReservationView original = mockReservationView(50L, "user-sub", 10L);
            when(original.getExpiresAt()).thenReturn(OffsetDateTime.now().minusMinutes(5));
            when(streetReservationStore.findById(50L)).thenReturn(Optional.of(original));

            assertThatThrownBy(() -> useCase.execute("user-sub", "user-sub", 50L, buildRequest(10L, 60)))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("no longer active");
        }
    }
}
