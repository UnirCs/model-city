package com.modelcity.leisure.cityroutes.store;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.cityplaces.repository.CityPlaceRepository;
import com.modelcity.leisure.cityplaces.repository.model.CityPlace;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteRequestDto;
import com.modelcity.leisure.cityroutes.repository.CityRouteRepository;
import com.modelcity.leisure.cityroutes.repository.model.CityRoute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultCityRouteStoreTest {

    @Mock
    CityRouteRepository<CityRoute> cityRouteRepository;

    @Mock
    CityPlaceRepository<CityPlace> cityPlaceRepository;

    DefaultCityRouteStore store;

    @BeforeEach
    void setUp() {
        store = new DefaultCityRouteStore(cityRouteRepository, cityPlaceRepository);
    }

    private CityRouteRequestDto buildRequest(List<Long> placeIds) {
        CityRouteRequestDto request = new CityRouteRequestDto();
        request.setName(Map.of("es", "Ruta Histórica", "en", "Historic Route"));
        request.setDescription(Map.of("es", "Descripción"));
        request.setTargetAudience("Familias");
        request.setImageUrl("route.jpg");
        request.setEstimatedDurationMinutes(120);
        request.setCityPlaceIds(placeIds);
        return request;
    }

    private CityPlace place(Long id) {
        CityPlace p = new CityPlace();
        p.setId(id);
        return p;
    }

    @Test
    void create_withValidPlaceIds_ordersRoutePlaces() {
        CityRouteRequestDto request = buildRequest(List.of(1L, 2L, 3L));
        when(cityPlaceRepository.findAllById(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(place(1L), place(2L), place(3L)));
        when(cityRouteRepository.save(any(CityRoute.class))).thenAnswer(inv -> inv.getArgument(0));

        CityRoute result = store.create(request);

        assertThat(result.getName()).isEqualTo("Ruta Histórica");
        assertThat(result.getRoutePlaces()).hasSize(3);
        assertThat(result.getRoutePlaces().get(0).getPlace().getId()).isEqualTo(1L);
        assertThat(result.getRoutePlaces().get(0).getSortOrder()).isEqualTo(0);
        assertThat(result.getRoutePlaces().get(2).getSortOrder()).isEqualTo(2);
    }

    @Test
    void create_withMissingPlaceId_throwsBadRequest() {
        CityRouteRequestDto request = buildRequest(List.of(1L, 99L));
        when(cityPlaceRepository.findAllById(List.of(1L, 99L))).thenReturn(List.of(place(1L)));

        assertThatThrownBy(() -> store.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("do not exist");
    }

    @Test
    void create_withoutPlaceIds_createsEmptyRoute() {
        CityRouteRequestDto request = buildRequest(List.of());
        when(cityRouteRepository.save(any(CityRoute.class))).thenAnswer(inv -> inv.getArgument(0));

        CityRoute result = store.create(request);

        assertThat(result.getRoutePlaces()).isEmpty();
        verify(cityPlaceRepository, never()).findAllById(anyList());
    }

    @Test
    void update_notFound_throwsResourceNotFound() {
        when(cityRouteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> store.update(99L, buildRequest(List.of())))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_existingRoute_clearsAndFlushesBeforeReapplying() {
        CityRoute existing = CityRoute.builder().routePlaces(new java.util.ArrayList<>()).build();
        when(cityRouteRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(cityRouteRepository.saveAndFlush(any(CityRoute.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cityRouteRepository.save(any(CityRoute.class))).thenAnswer(inv -> inv.getArgument(0));

        CityRoute result = store.update(1L, buildRequest(List.of()));

        verify(cityRouteRepository).saveAndFlush(existing);
        assertThat(result.getName()).isEqualTo("Ruta Histórica");
    }

    @Test
    void deleteById_delegatesToRepository() {
        store.deleteById(1L);
        verify(cityRouteRepository).deleteById(1L);
    }

    @Test
    void existsById_delegatesToRepository() {
        when(cityRouteRepository.existsById(1L)).thenReturn(true);
        assertThat(store.existsById(1L)).isTrue();
    }
}
