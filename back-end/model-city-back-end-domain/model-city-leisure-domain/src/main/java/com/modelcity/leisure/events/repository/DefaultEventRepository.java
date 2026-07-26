package com.modelcity.leisure.events.repository;

import com.modelcity.leisure.events.repository.model.Event;

/** Concrete Spring Data repository binding {@link EventRepository} to the platform's {@code Event}. */
public interface DefaultEventRepository extends EventRepository<Event> {
}
