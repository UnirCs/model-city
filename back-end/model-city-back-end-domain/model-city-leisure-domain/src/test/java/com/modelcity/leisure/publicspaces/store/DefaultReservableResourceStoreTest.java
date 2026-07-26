package com.modelcity.leisure.publicspaces.store;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceRequestDto;
import com.modelcity.leisure.publicspaces.repository.ReservableResourceRepository;
import com.modelcity.leisure.publicspaces.repository.model.ReservableResource;
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
class DefaultReservableResourceStoreTest {

    @Mock
    ReservableResourceRepository<ReservableResource> reservableResourceRepository;

    DefaultReservableResourceStore store;

    @BeforeEach
    void setUp() {
        store = new DefaultReservableResourceStore(reservableResourceRepository);
    }

    private ReservableResourceRequestDto buildRequest() {
        ReservableResourceRequestDto request = new ReservableResourceRequestDto();
        request.setName(Map.of("es", "Pista de Tenis 1", "en", "Tennis Court 1"));
        request.setDescription(Map.of("es", "Descripción"));
        request.setResourceType("TENNIS_COURT");
        return request;
    }

    @Test
    void create_buildsActiveResourceInPublicSpace() {
        when(reservableResourceRepository.save(any(ReservableResource.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservableResource result = store.create(1L, buildRequest());

        assertThat(result.getPublicSpaceId()).isEqualTo(1L);
        assertThat(result.isActive()).isTrue();
        assertThat(result.getName()).isEqualTo("Pista de Tenis 1");
        assertThat(result.getTranslations()).containsKey("en");
    }

    @Test
    void update_existingResource_appliesFields() {
        ReservableResource existing = ReservableResource.builder().build();
        when(reservableResourceRepository.findByIdAndPublicSpaceIdAndActiveTrue(10L, 1L))
                .thenReturn(Optional.of(existing));
        when(reservableResourceRepository.save(any(ReservableResource.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservableResource result = store.update(10L, 1L, buildRequest());

        assertThat(result.getName()).isEqualTo("Pista de Tenis 1");
    }

    @Test
    void update_notFound_throwsResourceNotFound() {
        when(reservableResourceRepository.findByIdAndPublicSpaceIdAndActiveTrue(99L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> store.update(99L, 1L, buildRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void softDelete_marksInactive() {
        ReservableResource existing = ReservableResource.builder().active(true).build();
        when(reservableResourceRepository.findByIdAndPublicSpaceIdAndActiveTrue(10L, 1L))
                .thenReturn(Optional.of(existing));
        when(reservableResourceRepository.save(any(ReservableResource.class))).thenAnswer(inv -> inv.getArgument(0));

        store.softDelete(10L, 1L);

        assertThat(existing.isActive()).isFalse();
    }

    @Test
    void softDelete_notFound_throwsResourceNotFound() {
        when(reservableResourceRepository.findByIdAndPublicSpaceIdAndActiveTrue(99L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> store.softDelete(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void softDeleteByPublicSpace_marksAllInactive() {
        ReservableResource r1 = ReservableResource.builder().active(true).build();
        ReservableResource r2 = ReservableResource.builder().active(true).build();
        when(reservableResourceRepository.findByPublicSpaceIdAndActiveTrueOrderByIdAsc(1L))
                .thenReturn(List.of(r1, r2));
        when(reservableResourceRepository.save(any(ReservableResource.class))).thenAnswer(inv -> inv.getArgument(0));

        store.softDeleteByPublicSpace(1L);

        assertThat(r1.isActive()).isFalse();
        assertThat(r2.isActive()).isFalse();
        verify(reservableResourceRepository, times(2)).save(any(ReservableResource.class));
    }

    @Test
    void findActiveByPublicSpace_delegatesToRepository() {
        store.findActiveByPublicSpace(1L, null);
        verify(reservableResourceRepository).findByPublicSpaceIdAndActiveTrueOrderByIdAsc(1L, null);
    }
}
