package com.modelcity.leisure.events.store;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Component;
import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.events.store.model.EventTicketView;
import com.modelcity.leisure.events.repository.EventTicketRepository;
import com.modelcity.leisure.events.repository.model.EventTicket;
import com.modelcity.leisure.events.repository.model.TicketStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** JPA adapter for the event ticket persistence port. */
@RequiredArgsConstructor
@Component
@ModelCityDisabledIfInherited
public class DefaultEventTicketStore implements EventTicketStore<EventTicket> {

    private final EventTicketRepository<EventTicket> eventTicketRepository;

    @Override
    public Optional<EventTicket> findById(Long id) {
        return eventTicketRepository.findById(id);
    }

    @Override
    public Page<EventTicket> findByEvent(Long eventId, Pageable pageable) {
        return eventTicketRepository.findByEventIdOrderByPurchasedAtAsc(eventId, pageable);
    }

    @Override
    public Page<EventTicket> findByCitizen(String citizenSub, Pageable pageable) {
        return eventTicketRepository.findByCitizenSubOrderByPurchasedAtDesc(citizenSub, pageable);
    }

    @Override
    public Page<EventTicket> findByCitizenAndEventStartBetween(
            String citizenSub, LocalDateTime startInclusive, LocalDateTime endExclusive, Pageable pageable) {
        return eventTicketRepository.findByCitizenSubAndEventStartBetween(citizenSub, startInclusive, endExclusive, pageable);
    }

    @Override
    public Page<EventTicket> findByCitizenAndEventStartAfter(
            String citizenSub, LocalDateTime date, Pageable pageable) {
        return eventTicketRepository.findByCitizenSubAndEventStartAfter(citizenSub, date, pageable);
    }

    @Override
    public Page<EventTicket> findByCitizenAndEventStartBefore(
            String citizenSub, LocalDateTime date, Pageable pageable) {
        return eventTicketRepository.findByCitizenSubAndEventStartBefore(citizenSub, date, pageable);
    }

    @Override
    public List<EventTicket> findByEventAndStatus(Long eventId, TicketStatus status) {
        return eventTicketRepository.findByEventIdAndStatus(eventId, status);
    }

    @Override
    public long countByEventAndStatusIn(Long eventId, Collection<TicketStatus> statuses) {
        return statuses.stream().mapToLong(s -> eventTicketRepository.countByEventIdAndStatus(eventId, s)).sum();
    }

    @Override
    public boolean existsByEventAndCitizenAndStatusIn(Long eventId, String citizenSub, Collection<TicketStatus> statuses) {
        return eventTicketRepository.existsByEventIdAndCitizenSubAndStatusIn(eventId, citizenSub, statuses);
    }

    @Override
    public Optional<EventTicket> findByCheckoutSessionId(String checkoutSessionId) {
        return eventTicketRepository.findByStripeCheckoutSessionId(checkoutSessionId);
    }

    @Override
    public EventTicket createPending(Long eventId, String citizenSub, String citizenName,
                                         BigDecimal pricePaid, String currency,
                                         String checkoutSessionId) {
        EventTicket ticket = EventTicket.builder()
                .eventId(eventId)
                .citizenSub(citizenSub)
                .citizenName(citizenName)
                .pricePaid(pricePaid)
                .currency(currency)
                .status(TicketStatus.PENDING)
                .stripeCheckoutSessionId(checkoutSessionId)
                .build();
        return eventTicketRepository.save(ticket);
    }

    @Override
    public void markStatus(Long ticketId, TicketStatus status) {
        EventTicket ticket = eventTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));
        ticket.setStatus(status);
        eventTicketRepository.save(ticket);
    }

    @Override
    public void markRefunded(Long ticketId, LocalDateTime refundedAt) {
        EventTicket ticket = eventTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));
        ticket.setStatus(TicketStatus.REFUNDED);
        ticket.setRefundedAt(refundedAt);
        eventTicketRepository.save(ticket);
    }
}
