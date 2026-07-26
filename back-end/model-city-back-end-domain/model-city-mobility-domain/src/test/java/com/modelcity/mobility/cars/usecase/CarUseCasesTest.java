package com.modelcity.mobility.cars.usecase;

import com.modelcity.mobility.cars.controller.model.CarDto;
import com.modelcity.mobility.cars.controller.model.CarRequestDto;
import com.modelcity.mobility.cars.store.CarStore;
import com.modelcity.mobility.cars.store.model.CarView;
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
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CarUseCasesTest {

    @Mock
    @SuppressWarnings("unchecked")
    CarStore<CarView, CarRequestDto> carStore;

    @Mock
    SystemTrailGenerator systemTrailGenerator;

    private CarView mockCarView(Long id, String plate, String ownerSub) {
        CarView view = mock(CarView.class);
        when(view.getId()).thenReturn(id);
        when(view.getLicensePlate()).thenReturn(plate);
        when(view.getOwnerSub()).thenReturn(ownerSub);
        when(view.getNickname()).thenReturn("Mi Coche");
        when(view.getBrand()).thenReturn("Toyota");
        when(view.getModel()).thenReturn("Corolla");
        when(view.getCreatedAt()).thenReturn(OffsetDateTime.now());
        return view;
    }

    @Nested
    class CreateCarTests {

        DefaultCreateCarUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultCreateCarUseCase(carStore, systemTrailGenerator);
        }

        @Test
        void execute_registersCarSuccessfully() {
            CarRequestDto request = new CarRequestDto();
            request.setLicensePlate("1234ABC");
            CarView saved = mockCarView(1L, "1234ABC", "user-sub");

            when(carStore.existsByLicensePlate("1234ABC")).thenReturn(false);
            when(carStore.create("user-sub", "1234ABC", request)).thenReturn(saved);

            CarDto result = useCase.execute("user-sub", "user-sub", request);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getLicensePlate()).isEqualTo("1234ABC");
            verify(systemTrailGenerator).carRegistered(saved);
        }

        @Test
        void execute_normalizesLicensePlateToUpperCase() {
            CarRequestDto request = new CarRequestDto();
            request.setLicensePlate("  4321xyz  ");
            CarView saved = mockCarView(2L, "4321XYZ", "user-sub");

            when(carStore.existsByLicensePlate("4321XYZ")).thenReturn(false);
            when(carStore.create("user-sub", "4321XYZ", request)).thenReturn(saved);

            useCase.execute("user-sub", "user-sub", request);

            verify(carStore).existsByLicensePlate("4321XYZ");
            verify(carStore).create("user-sub", "4321XYZ", request);
        }

        @Test
        void execute_throwsForbiddenWhenCallerMismatch() {
            CarRequestDto request = new CarRequestDto();
            request.setLicensePlate("1234ABC");

            assertThatThrownBy(() -> useCase.execute("other-user", "caller-sub", request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Caller does not match");
        }

        @Test
        void execute_throwsConflictWhenPlateAlreadyRegistered() {
            CarRequestDto request = new CarRequestDto();
            request.setLicensePlate("EXIS123");
            when(carStore.existsByLicensePlate("EXIS123")).thenReturn(true);

            assertThatThrownBy(() -> useCase.execute("user-sub", "user-sub", request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("already registered");
        }
    }

    @Nested
    class GetUserCarsTests {

        DefaultGetUserCarsUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetUserCarsUseCase(carStore);
        }

        @Test
        @SuppressWarnings("unchecked")
        void execute_returnsUserCars() {
            CarView car = mockCarView(1L, "1234ABC", "user-sub");
            Page<CarView> page = new PageImpl<>(List.of(car));
            doReturn(page).when(carStore).findByOwner(eq("user-sub"), any());

            Page<CarDto> result = useCase.execute("user-sub", "user-sub", PageRequest.of(0, 5));

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getLicensePlate()).isEqualTo("1234ABC");
        }

        @Test
        void execute_throwsForbiddenWhenCallerMismatch() {
            assertThatThrownBy(() -> useCase.execute("other-user", "caller-sub", PageRequest.of(0, 5)))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Caller does not match");
        }

        @Test
        @SuppressWarnings("unchecked")
        void execute_returnsEmptyPageWhenNoCars() {
            Page<CarView> page = new PageImpl<>(List.of());
            doReturn(page).when(carStore).findByOwner(eq("user-sub"), any());

            Page<CarDto> result = useCase.execute("user-sub", "user-sub", PageRequest.of(0, 5));

            assertThat(result.getTotalElements()).isZero();
        }
    }
}
