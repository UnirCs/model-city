package com.modelcity.leisure.cityplaces.store;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceRequestDto;
import com.modelcity.leisure.cityplaces.repository.CityPlaceRepository;
import com.modelcity.leisure.cityplaces.repository.model.CityPlace;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultCityPlaceStoreTest {

    @Mock
    CityPlaceRepository<CityPlace> cityPlaceRepository;

    DefaultCityPlaceStore store;

    @BeforeEach
    void setUp() {
        store = new DefaultCityPlaceStore(cityPlaceRepository);
    }

    private CityPlaceRequestDto buildRequest() {
        CityPlaceRequestDto request = new CityPlaceRequestDto();
        request.setName(Map.of("es", "Plaza Mayor", "en", "Main Square"));
        request.setDescription(Map.of("es", "Descripción", "en", "Description"));
        request.setAddress(Map.of("es", "Calle Mayor 1"));
        request.setLatitude(40.4);
        request.setLongitude(-3.7);
        request.setPhotoUrls(List.of("photo1.jpg", "photo2.jpg", "photo3.jpg", "photo4.jpg"));
        request.setAccessInfo(Map.of("es", "Acceso libre"));
        request.setAccessibilityInfo(Map.of("es", "Accesible"));
        request.setCategory("SQUARE");
        request.setVisitDurationMinutes(30);
        return request;
    }

    @Test
    void create_appliesFieldsAndPersists() {
        CityPlaceRequestDto request = buildRequest();
        when(cityPlaceRepository.save(any(CityPlace.class))).thenAnswer(inv -> inv.getArgument(0));

        CityPlace result = store.create(request);

        assertThat(result.getName()).isEqualTo("Plaza Mayor");
        assertThat(result.getDescription()).isEqualTo("Descripción");
        assertThat(result.getCategory()).isEqualTo("SQUARE");
        assertThat(result.getVisitDurationMinutes()).isEqualTo(30);
        assertThat(result.getTranslations()).containsKey("en");
        assertThat(result.getTranslations().get("en").getName()).isEqualTo("Main Square");
    }

    @Test
    void create_onlyKeepsFirstThreePhotos() {
        CityPlaceRequestDto request = buildRequest();
        when(cityPlaceRepository.save(any(CityPlace.class))).thenAnswer(inv -> inv.getArgument(0));

        CityPlace result = store.create(request);

        assertThat(result.getPhotoUrl1()).isEqualTo("photo1.jpg");
        assertThat(result.getPhotoUrl2()).isEqualTo("photo2.jpg");
        assertThat(result.getPhotoUrl3()).isEqualTo("photo3.jpg");
    }

    @Test
    void create_withoutPhotos_leavesPhotoFieldsNull() {
        CityPlaceRequestDto request = buildRequest();
        request.setPhotoUrls(null);
        when(cityPlaceRepository.save(any(CityPlace.class))).thenAnswer(inv -> inv.getArgument(0));

        CityPlace result = store.create(request);

        assertThat(result.getPhotoUrl1()).isNull();
        assertThat(result.getPhotoUrl2()).isNull();
        assertThat(result.getPhotoUrl3()).isNull();
    }

    @Test
    void create_missingDefaultLocaleName_throwsBadRequest() {
        CityPlaceRequestDto request = buildRequest();
        request.setName(Map.of("en", "Main Square"));

        assertThatThrownBy(() -> store.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("default locale");
    }

    @Test
    void update_existingPlace_appliesFields() {
        CityPlace existing = new CityPlace();
        when(cityPlaceRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(cityPlaceRepository.save(any(CityPlace.class))).thenAnswer(inv -> inv.getArgument(0));

        CityPlace result = store.update(1L, buildRequest());

        assertThat(result.getName()).isEqualTo("Plaza Mayor");
    }

    @Test
    void update_notFound_throwsResourceNotFound() {
        when(cityPlaceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> store.update(99L, buildRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAll_delegatesToRepository() {
        store.findAll(null);
        verify(cityPlaceRepository).findAll((org.springframework.data.domain.Pageable) null);
    }

    @Test
    void findByCategory_delegatesToRepository() {
        store.findByCategory("MUSEUM", null);
        verify(cityPlaceRepository).findByCategoryIgnoreCase("MUSEUM", null);
    }

    @Test
    void existsById_delegatesToRepository() {
        when(cityPlaceRepository.existsById(1L)).thenReturn(true);
        assertThat(store.existsById(1L)).isTrue();
    }

    @Test
    void deleteById_delegatesToRepository() {
        store.deleteById(1L);
        verify(cityPlaceRepository).deleteById(1L);
    }
}
