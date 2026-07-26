package com.modelcity.leisure.events.store;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;

import com.modelcity.leisure.events.controller.model.EventRequestDto;
import com.modelcity.leisure.events.store.model.EventView;
import com.modelcity.leisure.events.repository.model.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

/** Persistence port for events. */
@ModelCityExtensionPoint
public interface EventStore<T extends EventView, R extends EventRequestDto> {

    Optional<T> findActiveById(Long id);

    /** Active events filtered by optional type and paid flag, excluding past events (startsAt &gt;= now). */
    Page<T> search(EventType type, Boolean paid, LocalDateTime now, Pageable pageable);

    /** Builds and persists a new active event from the request (currency upper-cased; stripePriceId optional). */
    T create(R request, String stripePriceId);

    /** Applies the request onto the existing active event (assumed to exist) and persists it. */
    T update(Long id, R request);

    /** Soft-deletes the event (assumed to exist). */
    void softDelete(Long id);
}
