package com.modelcity.leisure.events.repository;

import com.modelcity.leisure.events.repository.model.EventTicket;

/** Concrete Spring Data repository binding {@link EventTicketRepository} to this topology's {@code EventTicket}. */
public interface DefaultEventTicketRepository extends EventTicketRepository<EventTicket> {
}
