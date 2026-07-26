package com.modelcity.leisure.publicspaces.usecase;

import com.modelcity.common.client.CoreClient;
import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceDto;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceRequestDto;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceSummaryDto;
import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceDto;
import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceRequestDto;
import com.modelcity.leisure.publicspaces.controller.model.ReservationDto;
import com.modelcity.leisure.publicspaces.controller.model.ReservationRequestDto;
import com.modelcity.leisure.publicspaces.model.PublicSpaceView;
import com.modelcity.leisure.publicspaces.model.ReservableResourceView;
import com.modelcity.leisure.publicspaces.model.SpaceReservationView;
import com.modelcity.leisure.publicspaces.store.PublicSpaceStore;
import com.modelcity.leisure.publicspaces.store.ReservableResourceStore;
import com.modelcity.leisure.publicspaces.store.SpaceReservationStore;
import com.modelcity.leisure.trails.SystemTrailGenerator;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PublicSpaceUseCasesTest {

    @Mock
    @SuppressWarnings("unchecked")
    PublicSpaceStore<PublicSpaceView, PublicSpaceRequestDto> publicSpaceStore;

    @Mock
    @SuppressWarnings("unchecked")
    ReservableResourceStore<ReservableResourceView, ReservableResourceRequestDto> reservableResourceStore;

    @Mock
    @SuppressWarnings("unchecked")
    SpaceReservationStore<SpaceReservationView> spaceReservationStore;

    @Mock
    SystemTrailGenerator systemTrailGenerator;

    @Mock
    CoreClient coreClient;

    private PublicSpaceView mockSpaceView(Long id, String name) {
        PublicSpaceView view = mock(PublicSpaceView.class);
        when(view.getId()).thenReturn(id);
        when(view.getName()).thenReturn(name);
        when(view.getTranslations()).thenReturn(Map.of());
        return view;
    }

    private ReservableResourceView mockResourceView(Long id, Long spaceId, String name) {
        ReservableResourceView view = mock(ReservableResourceView.class);
        when(view.getId()).thenReturn(id);
        when(view.getPublicSpaceId()).thenReturn(spaceId);
        when(view.getName()).thenReturn(name);
        when(view.getTranslations()).thenReturn(Map.of());
        return view;
    }

    private SpaceReservationView mockReservationView(Long id, Long resourceId) {
        SpaceReservationView view = mock(SpaceReservationView.class);
        when(view.getId()).thenReturn(id);
        when(view.getResourceId()).thenReturn(resourceId);
        when(view.getCitizenSub()).thenReturn("citizen-sub");
        when(view.getCitizenName()).thenReturn("Ciudadano Test");
        when(view.getReservationDate()).thenReturn(LocalDate.now());
        when(view.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(view.getEndTime()).thenReturn(LocalTime.of(11, 0));
        return view;
    }

    // ===================== PUBLIC SPACE USE CASES =====================

    @Nested
    class CreatePublicSpaceTests {

        DefaultCreatePublicSpaceUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultCreatePublicSpaceUseCase(publicSpaceStore, systemTrailGenerator);
        }

        @Test
        void execute_createsAndReturnsDto() {
            PublicSpaceView view = mockSpaceView(1L, "Polideportivo Norte");
            PublicSpaceRequestDto request = new PublicSpaceRequestDto();
            request.setName(Map.of("es", "Polideportivo Norte"));

            when(publicSpaceStore.create(request)).thenReturn(view);

            PublicSpaceDto result = useCase.execute("sub-agent", request, "es");

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Polideportivo Norte");
            verify(systemTrailGenerator).publicSpaceCreated("sub-agent", view);
        }
    }

    @Nested
    class DeletePublicSpaceTests {

        DefaultDeletePublicSpaceUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultDeletePublicSpaceUseCase(publicSpaceStore, reservableResourceStore, systemTrailGenerator);
        }

        @Test
        void execute_softDeletesSpaceAndCascadesToResources() {
            PublicSpaceView view = mockSpaceView(1L, "Parque");
            when(publicSpaceStore.findActiveById(1L)).thenReturn(Optional.of(view));

            useCase.execute(1L, "sub-agent");

            verify(publicSpaceStore).softDelete(1L);
            verify(reservableResourceStore).softDeleteByPublicSpace(1L);
            verify(systemTrailGenerator).publicSpaceDeleted("sub-agent", view);
        }

        @Test
        void execute_throwsWhenNotFound() {
            when(publicSpaceStore.findActiveById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(99L, "sub"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class GetPublicSpaceTests {

        DefaultGetPublicSpaceUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetPublicSpaceUseCase(publicSpaceStore);
        }

        @Test
        void execute_returnsDto() {
            PublicSpaceView view = mockSpaceView(1L, "Biblioteca");
            when(publicSpaceStore.findActiveById(1L)).thenReturn(Optional.of(view));

            PublicSpaceDto result = useCase.execute(1L, "es");

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        void execute_throwsWhenNotFound() {
            when(publicSpaceStore.findActiveById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(99L, "es"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class GetPublicSpacesTests {

        DefaultGetPublicSpacesUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetPublicSpacesUseCase(publicSpaceStore);
        }

        @Test
        @SuppressWarnings("unchecked")
        void execute_returnsPageOfSpaces() {
            PublicSpaceView view = mockSpaceView(1L, "Centro Cívico");
            Page<PublicSpaceView> page = new PageImpl<>(List.of(view));
            doReturn(page).when(publicSpaceStore).findActive(any(Pageable.class));

            Page<PublicSpaceSummaryDto> result = useCase.execute(0, "es");

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        }
    }

    // ===================== RESERVABLE RESOURCE USE CASES =====================

    @Nested
    class CreateReservableResourceTests {

        DefaultCreateReservableResourceUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultCreateReservableResourceUseCase(publicSpaceStore, reservableResourceStore, systemTrailGenerator);
        }

        @Test
        void execute_createsResourceInExistingSpace() {
            PublicSpaceView space = mockSpaceView(1L, "Polideportivo");
            ReservableResourceView resource = mockResourceView(10L, 1L, "Pista de Tenis 1");
            ReservableResourceRequestDto request = new ReservableResourceRequestDto();
            request.setName(Map.of("es", "Pista de Tenis 1"));

            when(publicSpaceStore.findActiveById(1L)).thenReturn(Optional.of(space));
            when(reservableResourceStore.create(1L, request)).thenReturn(resource);

            ReservableResourceDto result = useCase.execute(1L, "sub-agent", request, "es");

            assertThat(result.getId()).isEqualTo(10L);
            verify(systemTrailGenerator).reservableResourceCreated("sub-agent", resource);
        }

        @Test
        void execute_throwsWhenSpaceNotFound() {
            when(publicSpaceStore.findActiveById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(99L, "sub", new ReservableResourceRequestDto(), "es"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class DeleteReservableResourceTests {

        DefaultDeleteReservableResourceUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultDeleteReservableResourceUseCase(reservableResourceStore, systemTrailGenerator);
        }

        @Test
        void execute_softDeletesResource() {
            ReservableResourceView resource = mockResourceView(10L, 1L, "Pista");
            when(reservableResourceStore.findActiveByIdAndPublicSpace(10L, 1L))
                    .thenReturn(Optional.of(resource));

            useCase.execute(1L, 10L, "sub-agent");

            verify(reservableResourceStore).softDelete(10L, 1L);
            verify(systemTrailGenerator).reservableResourceDeleted("sub-agent", resource);
        }

        @Test
        void execute_throwsWhenNotFound() {
            when(reservableResourceStore.findActiveByIdAndPublicSpace(99L, 1L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(1L, 99L, "sub"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class GetReservableResourcesTests {

        DefaultGetReservableResourcesUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetReservableResourcesUseCase(publicSpaceStore, reservableResourceStore);
        }

        @Test
        @SuppressWarnings("unchecked")
        void execute_returnsResources() {
            PublicSpaceView space = mockSpaceView(1L, "Polideportivo");
            ReservableResourceView resource = mockResourceView(10L, 1L, "Pista");
            Page<ReservableResourceView> page = new PageImpl<>(List.of(resource));

            when(publicSpaceStore.findActiveById(1L)).thenReturn(Optional.of(space));
            doReturn(page).when(reservableResourceStore).findActiveByPublicSpace(eq(1L), any(Pageable.class));

            Page<ReservableResourceDto> result = useCase.execute(1L, PageRequest.of(0, 10), "es");

            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        void execute_throwsWhenSpaceNotFound() {
            when(publicSpaceStore.findActiveById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(99L, PageRequest.of(0, 10), "es"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ===================== RESERVATION USE CASES =====================

    @Nested
    class CreateReservationTests {

        DefaultCreateReservationUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultCreateReservationUseCase(reservableResourceStore, spaceReservationStore, coreClient, systemTrailGenerator);
        }

        @Test
        void execute_createsValidReservation() {
            ReservableResourceView resource = mockResourceView(10L, 1L, "Pista");
            SpaceReservationView saved = mockReservationView(100L, 10L);

            ReservationRequestDto request = new ReservationRequestDto();
            request.setReservationDate(LocalDate.now().plusDays(1));
            request.setStartTime(LocalTime.of(10, 0));
            request.setEndTime(LocalTime.of(11, 0));

            when(reservableResourceStore.findActiveByIdAndPublicSpace(10L, 1L))
                    .thenReturn(Optional.of(resource));
            when(spaceReservationStore.findByResourceAndDate(10L, request.getReservationDate()))
                    .thenReturn(List.of());
            when(spaceReservationStore.create(eq(10L), eq("citizen-sub"), any(),
                    eq(request.getReservationDate()), eq(LocalTime.of(10, 0)), eq(LocalTime.of(11, 0))))
                    .thenReturn(saved);

            ReservationDto result = useCase.execute(1L, 10L, "citizen-sub", request);

            assertThat(result.getId()).isEqualTo(100L);
            verify(systemTrailGenerator).spaceReservationCreated("citizen-sub", saved);
        }

        @Test
        void execute_throwsWhenResourceNotFound() {
            when(reservableResourceStore.findActiveByIdAndPublicSpace(99L, 1L))
                    .thenReturn(Optional.empty());

            ReservationRequestDto request = new ReservationRequestDto();
            request.setStartTime(LocalTime.of(10, 0));
            request.setEndTime(LocalTime.of(11, 0));

            assertThatThrownBy(() -> useCase.execute(1L, 99L, "citizen-sub", request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void execute_throwsWhenEndBeforeStart() {
            ReservableResourceView resource = mockResourceView(10L, 1L, "Pista");
            when(reservableResourceStore.findActiveByIdAndPublicSpace(10L, 1L))
                    .thenReturn(Optional.of(resource));

            ReservationRequestDto request = new ReservationRequestDto();
            request.setReservationDate(LocalDate.now().plusDays(1));
            request.setStartTime(LocalTime.of(12, 0));
            request.setEndTime(LocalTime.of(10, 0));

            assertThatThrownBy(() -> useCase.execute(1L, 10L, "citizen-sub", request))
                    .isInstanceOf(ResponseStatusException.class);
        }

        @Test
        void execute_throwsWhenTimeSlotOverlaps() {
            ReservableResourceView resource = mockResourceView(10L, 1L, "Pista");
            SpaceReservationView existing = mockReservationView(50L, 10L);

            when(reservableResourceStore.findActiveByIdAndPublicSpace(10L, 1L))
                    .thenReturn(Optional.of(resource));
            when(spaceReservationStore.findByResourceAndDate(eq(10L), any()))
                    .thenReturn(List.of(existing));

            ReservationRequestDto request = new ReservationRequestDto();
            request.setReservationDate(LocalDate.now().plusDays(1));
            request.setStartTime(LocalTime.of(10, 0));
            request.setEndTime(LocalTime.of(11, 0));

            assertThatThrownBy(() -> useCase.execute(1L, 10L, "citizen-sub", request))
                    .isInstanceOf(ResponseStatusException.class);
        }
    }

    @Nested
    class DeleteReservationTests {

        DefaultDeleteReservationUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultDeleteReservationUseCase(spaceReservationStore, systemTrailGenerator);
        }

        @Test
        void execute_deletesReservation() {
            SpaceReservationView reservation = mockReservationView(100L, 10L);
            when(spaceReservationStore.findById(100L)).thenReturn(Optional.of(reservation));

            useCase.execute(1L, 10L, 100L, "sub-agent");

            verify(spaceReservationStore).delete(100L);
            verify(systemTrailGenerator).spaceReservationDeleted("sub-agent", reservation);
        }

        @Test
        void execute_throwsWhenNotFound() {
            when(spaceReservationStore.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(1L, 10L, 99L, "sub"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void execute_throwsWhenReservationBelongsToOtherResource() {
            SpaceReservationView reservation = mockReservationView(100L, 999L);
            when(spaceReservationStore.findById(100L)).thenReturn(Optional.of(reservation));

            assertThatThrownBy(() -> useCase.execute(1L, 10L, 100L, "sub"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class GetReservationsTests {

        DefaultGetReservationsUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetReservationsUseCase(reservableResourceStore, spaceReservationStore, coreClient);
        }

        @Test
        @SuppressWarnings("unchecked")
        void execute_nonPrivileged_returnsPublicView() {
            ReservableResourceView resource = mockResourceView(10L, 1L, "Pista");
            SpaceReservationView reservation = mockReservationView(100L, 10L);
            Page<SpaceReservationView> page = new PageImpl<>(List.of(reservation));

            when(reservableResourceStore.findActiveByIdAndPublicSpace(10L, 1L))
                    .thenReturn(Optional.of(resource));
            doReturn(page).when(spaceReservationStore).findByResourceAndDate(eq(10L), any(), any(Pageable.class));
            when(coreClient.getUserRole("citizen-sub")).thenReturn("MODEL-CITY-CITIZEN");

            Page<ReservationDto> result = useCase.execute(1L, 10L, LocalDate.now(), "citizen-sub",
                    PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getCitizenSub()).isNull();
        }

        @Test
        void execute_throwsWhenResourceNotFound() {
            when(reservableResourceStore.findActiveByIdAndPublicSpace(99L, 1L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(1L, 99L, LocalDate.now(), "sub",
                    PageRequest.of(0, 10)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
