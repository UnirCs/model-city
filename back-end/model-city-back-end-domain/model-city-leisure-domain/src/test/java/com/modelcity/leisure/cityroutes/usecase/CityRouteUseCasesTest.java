package com.modelcity.leisure.cityroutes.usecase;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteDto;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteRequestDto;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteSummaryDto;
import com.modelcity.leisure.cityroutes.store.CityRouteStore;
import com.modelcity.leisure.cityroutes.store.model.CityRouteView;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CityRouteUseCasesTest {

    @Mock
    @SuppressWarnings("unchecked")
    CityRouteStore<CityRouteView, CityRouteRequestDto> cityRouteStore;

    @Mock
    SystemTrailGenerator systemTrailGenerator;

    private CityRouteView mockView(Long id, String name) {
        CityRouteView view = mock(CityRouteView.class);
        when(view.getId()).thenReturn(id);
        when(view.getName()).thenReturn(name);
        when(view.getTranslations()).thenReturn(Map.of());
        when(view.getRoutePlaces()).thenReturn(List.of());
        return view;
    }

    @Nested
    class CreateCityRouteTests {

        DefaultCreateCityRouteUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultCreateCityRouteUseCase(cityRouteStore, systemTrailGenerator);
        }

        @Test
        void execute_createsAndReturnsDto() {
            CityRouteView view = mockView(1L, "Ruta Histórica");
            CityRouteRequestDto request = new CityRouteRequestDto();
            request.setName(Map.of("es", "Ruta Histórica"));

            when(cityRouteStore.create(request)).thenReturn(view);

            CityRouteDto result = useCase.execute("sub-agent", request, "es");

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Ruta Histórica");
            verify(systemTrailGenerator).cityRouteCreated("sub-agent", view);
        }
    }

    @Nested
    class DeleteCityRouteTests {

        DefaultDeleteCityRouteUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultDeleteCityRouteUseCase(cityRouteStore, systemTrailGenerator);
        }

        @Test
        void execute_deletesWhenFound() {
            CityRouteView view = mockView(1L, "Ruta");
            when(cityRouteStore.findById(1L)).thenReturn(Optional.of(view));

            useCase.execute(1L, "sub-agent");

            verify(cityRouteStore).deleteById(1L);
            verify(systemTrailGenerator).cityRouteDeleted("sub-agent", view);
        }

        @Test
        void execute_throwsWhenNotFound() {
            when(cityRouteStore.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(99L, "sub-agent"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class GetCityRouteTests {

        DefaultGetCityRouteUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetCityRouteUseCase(cityRouteStore);
        }

        @Test
        void execute_returnsDto() {
            CityRouteView view = mockView(1L, "Ruta");
            when(cityRouteStore.findById(1L)).thenReturn(Optional.of(view));

            CityRouteDto result = useCase.execute(1L, "es");

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        void execute_throwsWhenNotFound() {
            when(cityRouteStore.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(99L, "es"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class GetCityRouteForEditTests {

        DefaultGetCityRouteForEditUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetCityRouteForEditUseCase(cityRouteStore);
        }

        @Test
        void execute_returnsFullDto() {
            CityRouteView view = mockView(1L, "Ruta");
            when(cityRouteStore.findById(1L)).thenReturn(Optional.of(view));

            CityRouteDto result = useCase.execute(1L, "es");

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        void execute_throwsWhenNotFound() {
            when(cityRouteStore.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(99L, "es"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class GetCityRoutesTests {

        DefaultGetCityRoutesUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetCityRoutesUseCase(cityRouteStore);
        }

        @Test
        void execute_returnsPageOfRoutes() {
            CityRouteView view = mockView(1L, "Ruta Cultural");
            Page<CityRouteView> page = new PageImpl<>(List.of(view));
            when(cityRouteStore.findAll(any(Pageable.class))).thenReturn(page);

            Page<CityRouteSummaryDto> result = useCase.execute(0, "es");

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        }
    }

    @Nested
    class UpdateCityRouteTests {

        DefaultUpdateCityRouteUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultUpdateCityRouteUseCase(cityRouteStore, systemTrailGenerator);
        }

        @Test
        void execute_updatesAndReturnsDto() {
            CityRouteView existing = mockView(1L, "Old Name");
            CityRouteView updated = mockView(1L, "New Name");
            CityRouteRequestDto request = new CityRouteRequestDto();
            request.setName(Map.of("es", "New Name"));

            when(cityRouteStore.findById(1L)).thenReturn(Optional.of(existing));
            when(cityRouteStore.update(1L, request)).thenReturn(updated);

            CityRouteDto result = useCase.execute(1L, "sub-agent", request, "es");

            assertThat(result.getName()).isEqualTo("New Name");
            verify(systemTrailGenerator).cityRouteUpdated("sub-agent", updated);
        }

        @Test
        void execute_throwsWhenNotFound() {
            when(cityRouteStore.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(99L, "sub-agent", new CityRouteRequestDto(), "es"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
