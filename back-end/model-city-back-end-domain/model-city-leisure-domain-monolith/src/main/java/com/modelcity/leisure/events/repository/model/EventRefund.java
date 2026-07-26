package com.modelcity.leisure.events.repository.model;

import com.modelcity.core.users.repository.model.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Monolith flavour of the event refund: adds a real read-only {@code @ManyToOne} navigation to {@link User}
 * (the staff member that triggered the refund) on top of the invariant mapping in {@link EventRefundBase}.
 * This relationship cannot exist in microservices — hence the topology-specific concrete entity.
 */
@Entity
@Table(name = "event_refunds")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class EventRefund extends EventRefundBase {

    /** Read-only navigation to the staff member that triggered the refund (nullable for SYSTEM). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by_sub", insertable = false, updatable = false)
    private User issuedBy;
}
