package com.modelcity.leisure.events.repository.model;

import com.modelcity.leisure.events.store.model.EventRefundView;
import jakarta.persistence.Column;
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
 * Topology-invariant mapping for an event refund. The concrete {@code @Entity EventRefund} lives in the
 * per-topology domain library and adds only the divergent part — the monolith's real {@code @ManyToOne}
 * navigation to {@code User} (the staff member that issued the refund), absent in microservices. Same
 * Phase-4 seam as {@link EventTicketBase}.
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class EventRefundBase implements EventRefundView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(length = 512)
    private String reason;

    @Column(nullable = false)
    private boolean automatic;

    @Column(name = "issued_by_sub")
    private String issuedBySub;

    @Column(name = "refunded_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime refundedAt;
}
