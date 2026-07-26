package com.modelcity.leisure.events.store;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.events.repository.EventTicketRepository;
import com.modelcity.leisure.events.repository.model.EventTicket;
import com.modelcity.leisure.events.repository.model.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultEventTicketStoreTest {

    @Mock
    EventTicketRepository<EventTicket> eventTicketRepository;

    DefaultEventTicketStore store;

    @BeforeEach
    void setUp() {
        store = new DefaultEventTicketStore(eventTicketRepository);
    }

    @Test
    void createPending_buildsPendingTicket() {
        when(eventTicketRepository.save(any(EventTicket.class))).thenAnswer(inv -> inv.getArgument(0));

        EventTicket result = store.createPending(1L, "citizen-sub", "Ciudadano", BigDecimal.TEN, "EUR", "cs_123");

        assertThat(result.getEventId()).isEqualTo(1L);
        assertThat(result.getCitizenSub()).isEqualTo("citizen-sub");
        assertThat(result.getStatus()).isEqualTo(TicketStatus.PENDING);
        assertThat(result.getStripeCheckoutSessionId()).isEqualTo("cs_123");
    }

    @Test
    void markStatus_updatesExistingTicket() {
        EventTicket ticket = EventTicket.builder().status(TicketStatus.PENDING).build();
        when(eventTicketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(eventTicketRepository.save(any(EventTicket.class))).thenAnswer(inv -> inv.getArgument(0));

        store.markStatus(10L, TicketStatus.PAID);

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.PAID);
    }

    @Test
    void markStatus_notFound_throwsResourceNotFound() {
        when(eventTicketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> store.markStatus(99L, TicketStatus.PAID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void markRefunded_setsStatusAndTimestamp() {
        EventTicket ticket = EventTicket.builder().status(TicketStatus.PAID).build();
        LocalDateTime refundedAt = LocalDateTime.now();
        when(eventTicketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(eventTicketRepository.save(any(EventTicket.class))).thenAnswer(inv -> inv.getArgument(0));

        store.markRefunded(10L, refundedAt);

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.REFUNDED);
        assertThat(ticket.getRefundedAt()).isEqualTo(refundedAt);
    }

    @Test
    void markRefunded_notFound_throwsResourceNotFound() {
        when(eventTicketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> store.markRefunded(99L, LocalDateTime.now()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void countByEventAndStatusIn_sumsAcrossAllStatuses() {
        when(eventTicketRepository.countByEventIdAndStatus(1L, TicketStatus.PURCHASED)).thenReturn(3L);
        when(eventTicketRepository.countByEventIdAndStatus(1L, TicketStatus.PAID)).thenReturn(2L);

        long total = store.countByEventAndStatusIn(1L, List.of(TicketStatus.PURCHASED, TicketStatus.PAID));

        assertThat(total).isEqualTo(5L);
    }

    @Test
    void countByEventAndStatusIn_emptyStatuses_returnsZero() {
        long total = store.countByEventAndStatusIn(1L, List.of());
        assertThat(total).isZero();
    }

    @Test
    void findByCheckoutSessionId_delegatesToRepository() {
        store.findByCheckoutSessionId("cs_123");
        verify(eventTicketRepository).findByStripeCheckoutSessionId("cs_123");
    }

    @Test
    void findByEvent_delegatesToRepository() {
        store.findByEvent(1L, null);
        verify(eventTicketRepository).findByEventIdOrderByPurchasedAtAsc(1L, null);
    }
}
