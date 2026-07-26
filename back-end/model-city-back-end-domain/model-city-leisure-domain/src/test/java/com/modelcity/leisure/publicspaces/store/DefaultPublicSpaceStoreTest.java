package com.modelcity.leisure.publicspaces.store;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceRequestDto;
import com.modelcity.leisure.publicspaces.repository.PublicSpaceRepository;
import com.modelcity.leisure.publicspaces.repository.model.PublicSpace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultPublicSpaceStoreTest {

    @Mock
    PublicSpaceRepository<PublicSpace> publicSpaceRepository;

    DefaultPublicSpaceStore store;

    @BeforeEach
    void setUp() {
        store = new DefaultPublicSpaceStore(publicSpaceRepository);
    }

    private PublicSpaceRequestDto buildRequest() {
        PublicSpaceRequestDto request = new PublicSpaceRequestDto();
        request.setName(Map.of("es", "Polideportivo", "en", "Sports Centre"));
        request.setDescription(Map.of("es", "Descripción"));
        request.setAddress(Map.of("es", "Calle Mayor 1"));
        request.setLatitude(40.4);
        request.setLongitude(-3.7);
        request.setPhotoUrls(List.of("p1.jpg", "p2.jpg"));
        return request;
    }

    @Test
    void create_setsActiveTrueAndAppliesFields() {
        when(publicSpaceRepository.save(any(PublicSpace.class))).thenAnswer(inv -> inv.getArgument(0));

        PublicSpace result = store.create(buildRequest());

        assertThat(result.isActive()).isTrue();
        assertThat(result.getName()).isEqualTo("Polideportivo");
        assertThat(result.getAddress()).isEqualTo("Calle Mayor 1");
        assertThat(result.getTranslations()).containsKey("en");
    }

    @Test
    void create_photosCappedAtThree() {
        PublicSpaceRequestDto request = buildRequest();
        request.setPhotoUrls(List.of("p1.jpg", "p2.jpg", "p3.jpg", "p4.jpg"));
        when(publicSpaceRepository.save(any(PublicSpace.class))).thenAnswer(inv -> inv.getArgument(0));

        PublicSpace result = store.create(request);

        assertThat(result.getPhotoUrl1()).isEqualTo("p1.jpg");
        assertThat(result.getPhotoUrl2()).isEqualTo("p2.jpg");
        assertThat(result.getPhotoUrl3()).isEqualTo("p3.jpg");
    }

    @Test
    void update_activeSpace_appliesFields() {
        PublicSpace existing = new PublicSpace();
        when(publicSpaceRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));
        when(publicSpaceRepository.save(any(PublicSpace.class))).thenAnswer(inv -> inv.getArgument(0));

        PublicSpace result = store.update(1L, buildRequest());

        assertThat(result.getName()).isEqualTo("Polideportivo");
    }

    @Test
    void update_notFound_throwsResourceNotFound() {
        when(publicSpaceRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> store.update(99L, buildRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void softDelete_marksInactive() {
        PublicSpace existing = new PublicSpace();
        existing.setActive(true);
        when(publicSpaceRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));
        when(publicSpaceRepository.save(any(PublicSpace.class))).thenAnswer(inv -> inv.getArgument(0));

        store.softDelete(1L);

        assertThat(existing.isActive()).isFalse();
    }

    @Test
    void softDelete_notFound_throwsResourceNotFound() {
        when(publicSpaceRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> store.softDelete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findActive_delegatesToRepository() {
        store.findActive(null);
        verify(publicSpaceRepository).findByActiveTrue(null);
    }

    @Test
    void findActiveById_delegatesToRepository() {
        store.findActiveById(1L);
        verify(publicSpaceRepository).findByIdAndActiveTrue(1L);
    }
}
