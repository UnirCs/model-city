package com.modelcity.leisure.events.store;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;

import com.modelcity.leisure.events.store.model.EventTicketView;
import com.modelcity.leisure.events.repository.model.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Persistence port for event tickets. */
@ModelCityExtensionPoint
public interface EventTicketStore<T extends EventTicketView> {

    Optional<T> findById(Long id);

    Page<T> findByEvent(Long eventId, Pageable pageable);

    Page<T> findByCitizen(String citizenSub, Pageable pageable);

    Page<T> findByCitizenAndEventStartBetween(
            String citizenSub, LocalDateTime startInclusive, LocalDateTime endExclusive, Pageable pageable);

    Page<T> findByCitizenAndEventStartAfter(
            String citizenSub, LocalDateTime date, Pageable pageable);

    Page<T> findByCitizenAndEventStartBefore(
            String citizenSub, LocalDateTime date, Pageable pageable);

    List<T> findByEventAndStatus(Long eventId, TicketStatus status);

    long countByEventAndStatusIn(Long eventId, Collection<TicketStatus> statuses);

    boolean existsByEventAndCitizenAndStatusIn(Long eventId, String citizenSub, Collection<TicketStatus> statuses);

    Optional<T> findByCheckoutSessionId(String checkoutSessionId);

    /** Persists a new PENDING ticket for the web Checkout Session flow. */
    T createPending(Long eventId, String citizenSub, String citizenName,
                                  BigDecimal pricePaid, String currency,
                                  String checkoutSessionId);

    /** Updates only the status of the ticket (used by webhook confirmations). */
    void markStatus(Long ticketId, TicketStatus status);

    /** Marks the ticket as REFUNDED with the given timestamp. */
    void markRefunded(Long ticketId, LocalDateTime refundedAt);
}
