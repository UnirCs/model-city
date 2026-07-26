package com.modelcity.leisure.publicspaces.store;

import com.modelcity.leisure.publicspaces.repository.SpaceReservationRepository;
import com.modelcity.leisure.publicspaces.repository.model.SpaceReservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultSpaceReservationStoreTest {

    @Mock
    SpaceReservationRepository<SpaceReservation> spaceReservationRepository;

    DefaultSpaceReservationStore store;

    @BeforeEach
    void setUp() {
        store = new DefaultSpaceReservationStore(spaceReservationRepository);
    }

    @Test
    void create_buildsReservationWithAllFields() {
        when(spaceReservationRepository.save(any(SpaceReservation.class))).thenAnswer(inv -> inv.getArgument(0));
        LocalDate date = LocalDate.of(2026, 8, 1);

        SpaceReservation result = store.create(10L, "citizen-sub", "Ciudadano", date,
                LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertThat(result.getResourceId()).isEqualTo(10L);
        assertThat(result.getCitizenSub()).isEqualTo("citizen-sub");
        assertThat(result.getCitizenName()).isEqualTo("Ciudadano");
        assertThat(result.getReservationDate()).isEqualTo(date);
        assertThat(result.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(result.getEndTime()).isEqualTo(LocalTime.of(11, 0));
    }

    @Test
    void delete_delegatesToRepository() {
        store.delete(100L);
        verify(spaceReservationRepository).deleteById(100L);
    }

    @Test
    void findByResourceAndDate_delegatesToRepository() {
        LocalDate date = LocalDate.of(2026, 8, 1);
        store.findByResourceAndDate(10L, date);
        verify(spaceReservationRepository).findByResourceIdAndReservationDateOrderByStartTimeAsc(10L, date);
    }

    @Test
    void findByResourceAndDatePaginated_delegatesToRepository() {
        LocalDate date = LocalDate.of(2026, 8, 1);
        store.findByResourceAndDate(10L, date, null);
        verify(spaceReservationRepository).findByResourceIdAndReservationDateOrderByStartTimeAsc(10L, date, null);
    }

    @Test
    void findById_delegatesToRepository() {
        store.findById(1L);
        verify(spaceReservationRepository).findById(1L);
    }
}
