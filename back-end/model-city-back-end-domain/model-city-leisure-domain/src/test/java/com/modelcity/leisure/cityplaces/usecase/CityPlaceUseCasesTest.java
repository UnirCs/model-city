package com.modelcity.leisure.cityplaces.usecase;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceDto;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceRequestDto;
import com.modelcity.leisure.cityplaces.store.CityPlaceStore;
import com.modelcity.leisure.cityplaces.store.model.CityPlaceView;
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
import org.springframework.data.domain.Pageable;

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
class CityPlaceUseCasesTest {

    @Mock
    @SuppressWarnings("unchecked")
    CityPlaceStore<CityPlaceView, CityPlaceRequestDto> cityPlaceStore;

    @Mock
    SystemTrailGenerator systemTrailGenerator;

    private CityPlaceView mockView(Long id, String name) {
        CityPlaceView view = mock(CityPlaceView.class);
        when(view.getId()).thenReturn(id);
        when(view.getName()).thenReturn(name);
        when(view.getTranslations()).thenReturn(Map.of());
        return view;
    }

    @Nested
    class CreateCityPlaceTests {

        DefaultCreateCityPlaceUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultCreateCityPlaceUseCase(cityPlaceStore, systemTrailGenerator);
        }

        @Test
        void execute_savesAndReturnsDto() {
            CityPlaceView view = mockView(1L, "Plaza Mayor");
            CityPlaceRequestDto request = new CityPlaceRequestDto();
            request.setName(Map.of("es", "Plaza Mayor"));

            when(cityPlaceStore.create(request)).thenReturn(view);

            CityPlaceDto result = useCase.execute("sub-agent", request, "es");

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Plaza Mayor");
            verify(systemTrailGenerator).cityPlaceCreated("sub-agent", view);
        }
    }

    @Nested
    class DeleteCityPlaceTests {

        DefaultDeleteCityPlaceUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultDeleteCityPlaceUseCase(cityPlaceStore, systemTrailGenerator);
        }

        @Test
        void execute_deletesWhenFound() {
            CityPlaceView view = mockView(1L, "Plaza");
            when(cityPlaceStore.findById(1L)).thenReturn(Optional.of(view));

            useCase.execute(1L, "sub-agent");

            verify(cityPlaceStore).deleteById(1L);
            verify(systemTrailGenerator).cityPlaceDeleted("sub-agent", view);
        }

        @Test
        void execute_throwsWhenNotFound() {
            when(cityPlaceStore.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(99L, "sub-agent"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class GetCityPlaceTests {

        DefaultGetCityPlaceUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetCityPlaceUseCase(cityPlaceStore);
        }

        @Test
        void execute_returnsDto() {
            CityPlaceView view = mockView(1L, "Plaza Mayor");
            when(cityPlaceStore.findById(1L)).thenReturn(Optional.of(view));

            CityPlaceDto result = useCase.execute(1L, "es");

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        void execute_throwsWhenNotFound() {
            when(cityPlaceStore.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(99L, "es"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class GetCityPlaceForEditTests {

        DefaultGetCityPlaceForEditUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetCityPlaceForEditUseCase(cityPlaceStore);
        }

        @Test
        void execute_returnsFullDto() {
            CityPlaceView view = mockView(1L, "Plaza");
            when(cityPlaceStore.findById(1L)).thenReturn(Optional.of(view));

            CityPlaceDto result = useCase.execute(1L, "es");

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        void execute_throwsWhenNotFound() {
            when(cityPlaceStore.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(99L, "es"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class GetCityPlacesTests {

        DefaultGetCityPlacesUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetCityPlacesUseCase(cityPlaceStore);
        }

        @Test
        void execute_withoutCategory_returnsAllPlaces() {
            CityPlaceView view = mockView(1L, "Parque");
            Page<CityPlaceView> page = new PageImpl<>(List.of(view));
            when(cityPlaceStore.findAll(any(Pageable.class))).thenReturn(page);

            Page<CityPlaceDto> result = useCase.execute(null, 0, "es");

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        }

        @Test
        void execute_withCategory_filtersByCategory() {
            CityPlaceView view = mockView(2L, "Museo");
            Page<CityPlaceView> page = new PageImpl<>(List.of(view));
            when(cityPlaceStore.findByCategory(eq("MUSEUM"), any(Pageable.class))).thenReturn(page);

            Page<CityPlaceDto> result = useCase.execute("MUSEUM", 0, "es");

            assertThat(result.getContent().get(0).getId()).isEqualTo(2L);
            verify(cityPlaceStore, never()).findAll(any());
        }

        @Test
        void execute_withBlankCategory_returnsAllPlaces() {
            CityPlaceView view = mockView(1L, "Parque");
            Page<CityPlaceView> page = new PageImpl<>(List.of(view));
            when(cityPlaceStore.findAll(any(Pageable.class))).thenReturn(page);

            Page<CityPlaceDto> result = useCase.execute("  ", 0, "es");

            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(cityPlaceStore, never()).findByCategory(any(), any());
        }
    }

    @Nested
    class UpdateCityPlaceTests {

        DefaultUpdateCityPlaceUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultUpdateCityPlaceUseCase(cityPlaceStore, systemTrailGenerator);
        }

        @Test
        void execute_updatesAndReturnsDto() {
            CityPlaceView existing = mockView(1L, "Old Name");
            CityPlaceView updated = mockView(1L, "New Name");
            CityPlaceRequestDto request = new CityPlaceRequestDto();
            request.setName(Map.of("es", "New Name"));

            when(cityPlaceStore.findById(1L)).thenReturn(Optional.of(existing));
            when(cityPlaceStore.update(1L, request)).thenReturn(updated);

            CityPlaceDto result = useCase.execute(1L, "sub-agent", request, "es");

            assertThat(result.getName()).isEqualTo("New Name");
            verify(systemTrailGenerator).cityPlaceUpdated("sub-agent", updated);
        }

        @Test
        void execute_throwsWhenNotFound() {
            when(cityPlaceStore.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(99L, "sub-agent", new CityPlaceRequestDto(), "es"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
