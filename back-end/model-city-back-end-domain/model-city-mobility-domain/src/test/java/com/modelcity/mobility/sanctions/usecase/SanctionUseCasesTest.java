package com.modelcity.mobility.sanctions.usecase;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.mobility.cars.controller.model.CarRequestDto;
import com.modelcity.mobility.cars.store.CarStore;
import com.modelcity.mobility.cars.store.model.CarView;
import com.modelcity.mobility.sanctions.controller.model.SanctionDto;
import com.modelcity.mobility.sanctions.controller.model.SanctionRequestDto;
import com.modelcity.mobility.sanctions.controller.model.SanctionSummaryDto;
import com.modelcity.mobility.sanctions.store.SanctionStore;
import com.modelcity.mobility.sanctions.store.model.SanctionView;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SanctionUseCasesTest {

    @Mock
    @SuppressWarnings("unchecked")
    SanctionStore<SanctionView, SanctionRequestDto> sanctionStore;

    @Mock
    @SuppressWarnings("unchecked")
    CarStore<CarView, CarRequestDto> carStore;

    @Mock
    SystemTrailGenerator systemTrailGenerator;

    private SanctionView mockSanctionView(Long id, String plate) {
        SanctionView view = mock(SanctionView.class);
        when(view.getId()).thenReturn(id);
        when(view.getLicensePlate()).thenReturn(plate);
        when(view.getLatitude()).thenReturn(40.0);
        when(view.getLongitude()).thenReturn(-3.0);
        when(view.getAgentSub()).thenReturn("agent-sub");
        when(view.getCreatedAt()).thenReturn(OffsetDateTime.now());
        when(view.getImageBase64()).thenReturn("base64data");
        return view;
    }

    private CarView mockCarView(String plate, String ownerSub) {
        CarView car = mock(CarView.class);
        when(car.getLicensePlate()).thenReturn(plate);
        when(car.getOwnerSub()).thenReturn(ownerSub);
        return car;
    }

    @Nested
    class CreateSanctionTests {

        DefaultCreateSanctionUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultCreateSanctionUseCase(sanctionStore, systemTrailGenerator);
        }

        @Test
        void execute_createsSanctionAndAudits() {
            SanctionRequestDto request = new SanctionRequestDto("1234ABC", 40.0, -3.0, "imgdata");
            SanctionView saved = mockSanctionView(1L, "1234ABC");

            when(sanctionStore.create("agent-sub", request)).thenReturn(saved);

            SanctionDto result = useCase.execute("agent-sub", request);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getLicensePlate()).isEqualTo("1234ABC");
            verify(systemTrailGenerator).sanctionIssued(saved);
        }
    }

    @Nested
    class GetSanctionTests {

        DefaultGetSanctionUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetSanctionUseCase(sanctionStore);
        }

        @Test
        void execute_returnsSanctionById() {
            SanctionView view = mockSanctionView(1L, "1234ABC");
            when(sanctionStore.findById(1L)).thenReturn(Optional.of(view));

            SanctionDto result = useCase.execute(1L);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        void execute_throwsWhenNotFound() {
            when(sanctionStore.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class GetSanctionsTests {

        DefaultGetSanctionsUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetSanctionsUseCase(sanctionStore);
        }

        @Test
        @SuppressWarnings("unchecked")
        void execute_returnsPageOfSanctions() {
            SanctionView view = mockSanctionView(1L, "1234ABC");
            Page<SanctionView> page = new PageImpl<>(List.of(view));
            doReturn(page).when(sanctionStore).search(any(), any(), any(), any(Pageable.class));

            Page<SanctionSummaryDto> result = useCase.execute(null, null, null, 0);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getLicensePlate()).isEqualTo("1234ABC");
        }

        @Test
        @SuppressWarnings("unchecked")
        void execute_withPlateFilter_passesItToStore() {
            Page<SanctionView> emptyPage = new PageImpl<>(List.of());
            doReturn(emptyPage).when(sanctionStore).search(eq("1234ABC"), any(), any(), any(Pageable.class));

            Page<SanctionSummaryDto> result = useCase.execute("1234ABC", null, null, 0);

            verify(sanctionStore).search(eq("1234ABC"), any(), any(), any(Pageable.class));
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    class GetUserSanctionTests {

        DefaultGetUserSanctionUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetUserSanctionUseCase(carStore, sanctionStore);
        }

        @Test
        void execute_returnsSanctionWhenPlateMatchesCitizenscar() {
            SanctionView sanction = mockSanctionView(1L, "1234ABC");
            CarView car = mockCarView("1234ABC", "user-sub");

            when(sanctionStore.findById(1L)).thenReturn(Optional.of(sanction));
            doReturn(List.of(car)).when(carStore).findByOwner("user-sub");

            SanctionDto result = useCase.execute("user-sub", "user-sub", 1L);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        void execute_throwsForbiddenWhenCallerMismatch() {
            assertThatThrownBy(() -> useCase.execute("other-user", "caller-sub", 1L))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Caller does not match");
        }

        @Test
        void execute_throwsNotFoundWhenSanctionMissing() {
            when(sanctionStore.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute("user-sub", "user-sub", 99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void execute_throwsForbiddenWhenSanctionBelongsToOtherPlate() {
            SanctionView sanction = mockSanctionView(1L, "OTHER99");
            CarView car = mockCarView("1234ABC", "user-sub");

            when(sanctionStore.findById(1L)).thenReturn(Optional.of(sanction));
            doReturn(List.of(car)).when(carStore).findByOwner("user-sub");

            assertThatThrownBy(() -> useCase.execute("user-sub", "user-sub", 1L))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("does not belong");
        }
    }

    @Nested
    class GetUserSanctionsTests {

        DefaultGetUserSanctionsUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetUserSanctionsUseCase(carStore, sanctionStore);
        }

        @Test
        @SuppressWarnings("unchecked")
        void execute_returnsSanctionsForUserPlates() {
            CarView car = mockCarView("1234ABC", "user-sub");
            SanctionView sanction = mockSanctionView(1L, "1234ABC");
            Page<SanctionView> page = new PageImpl<>(List.of(sanction));

            doReturn(List.of(car)).when(carStore).findByOwner("user-sub");
            doReturn(page).when(sanctionStore).findByPlatesIn(anyList(), any(Pageable.class));

            Page<SanctionSummaryDto> result = useCase.execute("user-sub", "user-sub", PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        void execute_returnsEmptyPageWhenNoCars() {
            doReturn(List.of()).when(carStore).findByOwner("user-sub");

            Page<SanctionSummaryDto> result = useCase.execute("user-sub", "user-sub", PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isZero();
            verify(sanctionStore, never()).findByPlatesIn(anyList(), any());
        }

        @Test
        void execute_throwsForbiddenWhenCallerMismatch() {
            assertThatThrownBy(() -> useCase.execute("other-user", "caller-sub", PageRequest.of(0, 10)))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Caller does not match");
        }
    }
}
