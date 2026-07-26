package com.modelcity.leisure.events.repository.model;

import com.modelcity.leisure.events.store.model.EventTicketView;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Topology-invariant mapping for an event ticket: every column identical in the monolith and the
 * microservices. The concrete {@code @Entity EventTicket} lives in the per-topology domain library
 * ({@code model-city-leisure-domain-monolith} / {@code -microservices}) and adds only the part that differs
 * — e.g. the monolith's real {@code @ManyToOne} navigation to {@code User}, which the microservices flavour
 * cannot have. This is the Phase-4 seam (§3): the shared base lives once, the divergence in the subclass.
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class EventTicketBase implements EventTicketView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "citizen_sub", nullable = false)
    private String citizenSub;

    @Column(name = "citizen_name")
    private String citizenName;

    @Column(name = "price_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePaid;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TicketStatus status;

    @Column(name = "purchased_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime purchasedAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "stripe_checkout_session_id", length = 255)
    private String stripeCheckoutSessionId;
}
