package com.modelcity.leisure.events.repository;

import com.modelcity.leisure.events.repository.model.EventTicketBase;
import com.modelcity.leisure.events.repository.model.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Generic over the concrete {@link EventTicketBase} subclass so both topology libraries
 * ({@code model-city-leisure-domain-microservices} / {@code -monolith}) — and any city that declares its own
 * entity extending {@code EventTicketBase} — reuse this contract instead of forking it. Marked
 * {@code @NoRepositoryBean}: Spring Data cannot proxy an unbound generic repository, so each topology exposes
 * the platform default through its own {@code DefaultEventTicketRepository}, binding {@code T} to its local
 * {@code EventTicket} entity.
 */
@NoRepositoryBean
public interface EventTicketRepository<T extends EventTicketBase> extends JpaRepository<T, Long> {

    Page<T> findByEventIdOrderByPurchasedAtAsc(Long eventId, Pageable pageable);

    Page<T> findByCitizenSubOrderByPurchasedAtDesc(String citizenSub, Pageable pageable);

    List<T> findByEventIdAndStatus(Long eventId, TicketStatus status);

    long countByEventIdAndStatus(Long eventId, TicketStatus status);

    Optional<T> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);

    boolean existsByEventIdAndCitizenSubAndStatusIn(Long eventId, String citizenSub, Collection<TicketStatus> statuses);

    @Query("""
            SELECT t FROM #{#entityName} t
            JOIN Event e ON t.eventId = e.id
            WHERE t.citizenSub = :citizenSub
              AND e.startsAt >= :startDate
              AND e.startsAt < :endDate
            ORDER BY t.purchasedAt DESC
            """)
    Page<T> findByCitizenSubAndEventStartBetween(
            @Param("citizenSub") String citizenSub,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("""
            SELECT t FROM #{#entityName} t
            JOIN Event e ON t.eventId = e.id
            WHERE t.citizenSub = :citizenSub
              AND e.startsAt < :date
            ORDER BY t.purchasedAt DESC
            """)
    Page<T> findByCitizenSubAndEventStartBefore(
            @Param("citizenSub") String citizenSub,
            @Param("date") LocalDateTime date,
            Pageable pageable);

    @Query("""
            SELECT t FROM #{#entityName} t
            JOIN Event e ON t.eventId = e.id
            WHERE t.citizenSub = :citizenSub
              AND e.startsAt >= :date
            ORDER BY t.purchasedAt DESC
            """)
    Page<T> findByCitizenSubAndEventStartAfter(
            @Param("citizenSub") String citizenSub,
            @Param("date") LocalDateTime date,
            Pageable pageable);
}
