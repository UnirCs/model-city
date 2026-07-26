package com.modelcity.leisure.events.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Microservices flavour of the event ticket: the citizen is referenced softly via the {@code citizenSub}
 * column inherited from {@link EventTicketBase}; there is no JPA relationship because the leisure
 * microservice's persistence unit has no {@code User} entity. The whole mapping is inherited — the archetype
 * ships only this thin subclass.
 */
@Entity
@Table(name = "event_tickets")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class EventTicket extends EventTicketBase {
}
