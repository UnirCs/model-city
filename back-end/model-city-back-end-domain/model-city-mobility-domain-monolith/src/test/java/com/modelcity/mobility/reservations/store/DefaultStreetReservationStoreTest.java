package com.modelcity.mobility.reservations.store;

import com.modelcity.mobility.reservations.repository.StreetReservationRepository;
import com.modelcity.mobility.reservations.repository.model.ReservationStatus;
import com.modelcity.mobility.reservations.repository.model.StreetReservation;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultStreetReservationStoreTest {

    @Mock
    StreetReservationRepository<StreetReservation> streetReservationRepository;

    @Mock
    EntityManager entityManager;

    DefaultStreetReservationStore store;

    @BeforeEach
    void setUp() {
        store = new DefaultStreetReservationStore(streetReservationRepository);
        store.entityManager = entityManager;
    }

    @Test
    void create_buildsPendingReservationWithAllFields() {
        when(streetReservationRepository.save(any(StreetReservation.class))).thenAnswer(inv -> inv.getArgument(0));
        OffsetDateTime now = OffsetDateTime.now();

        StreetReservation result = store.create("user-sub", 10L, 40.4, -3.7, now, now.plusHours(2),
                null, "cs_123", BigDecimal.TEN);

        assertThat(result.getUserSub()).isEqualTo("user-sub");
        assertThat(result.getCarId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(result.getStripeCheckoutSessionId()).isEqualTo("cs_123");
        assertThat(result.getPricePaid()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    @SuppressWarnings("unchecked")
    void search_withActiveFilter_delegatesWithSpecification() {
        Page<StreetReservation> page = new PageImpl<>(List.of());
        Pageable pageable = PageRequest.of(0, 20);
        doReturn(page).when(streetReservationRepository).findAll(any(Specification.class), eq(pageable));

        Page<StreetReservation> result = store.search("1234ABC", null, null, true, pageable);

        assertThat(result).isNotNull();
        verify(streetReservationRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @SuppressWarnings("unchecked")
    void search_withDateWindow_delegatesWithSpecification() {
        Page<StreetReservation> page = new PageImpl<>(List.of());
        Pageable pageable = PageRequest.of(0, 20);
        OffsetDateTime from = OffsetDateTime.now().minusDays(1);
        OffsetDateTime to = OffsetDateTime.now();
        doReturn(page).when(streetReservationRepository).findAll(any(Specification.class), eq(pageable));

        Page<StreetReservation> result = store.search(null, from, to, null, pageable);

        assertThat(result).isNotNull();
        verify(streetReservationRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void markStatusByCheckoutSession_found_updatesStatus() {
        StreetReservation reservation = StreetReservation.builder().status(ReservationStatus.PENDING).build();
        when(streetReservationRepository.findByStripeCheckoutSessionId("cs_123")).thenReturn(Optional.of(reservation));
        when(streetReservationRepository.save(any(StreetReservation.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<StreetReservation> result = store.markStatusByCheckoutSession("cs_123", ReservationStatus.PAID);

        assertThat(result).isPresent();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PAID);
        verify(streetReservationRepository).save(reservation);
    }

    @Test
    void markStatusByCheckoutSession_notFound_returnsEmpty() {
        when(streetReservationRepository.findByStripeCheckoutSessionId("cs_unknown")).thenReturn(Optional.empty());

        Optional<StreetReservation> result = store.markStatusByCheckoutSession("cs_unknown", ReservationStatus.PAID);

        assertThat(result).isEmpty();
        verify(streetReservationRepository, never()).save(any());
    }

    @Test
    void findUserHistory_delegatesToRepository() {
        OffsetDateTime from = OffsetDateTime.now().minusDays(30);
        store.findUserHistory("user-sub", from, null);
        verify(streetReservationRepository).findByUserSubAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
                "user-sub", from, null);
    }
}
