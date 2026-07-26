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
 * Monolith flavour of the event ticket: because the monolith's single persistence unit owns the {@code users}
 * table, it adds a real read-only {@code @ManyToOne} navigation to {@link User} on top of the invariant
 * mapping in {@link EventTicketBase}. This relationship cannot exist in the microservices flavour — exactly
 * why the concrete entity is topology-specific and only the base is shared (§3 / Phase 4).
 */
@Entity
@Table(name = "event_tickets")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class EventTicket extends EventTicketBase {

    /** Read-only navigation to the citizen owner of the ticket. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_sub", insertable = false, updatable = false)
    private User citizen;
}
