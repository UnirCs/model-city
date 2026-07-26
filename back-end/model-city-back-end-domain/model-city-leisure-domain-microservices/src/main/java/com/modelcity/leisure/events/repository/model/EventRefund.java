package com.modelcity.leisure.events.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Microservices flavour of the event refund: the issuing staff member is referenced softly via the
 * {@code issuedBySub} column inherited from {@link EventRefundBase}; no JPA relationship, because the leisure
 * microservice has no {@code User} entity. Thin subclass — the whole mapping is inherited.
 */
@Entity
@Table(name = "event_refunds")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class EventRefund extends EventRefundBase {
}
