package com.modelcity.leisure.events.usecase;
import com.modelcity.leisure.events.store.model.EventView;
import com.modelcity.leisure.events.controller.model.EventRequestDto;

import com.modelcity.common.config.cache.CacheNames;
import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.events.controller.model.EventDto;
import com.modelcity.leisure.events.store.EventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Caches the user-agnostic event detail, shared across all callers. The {@code acquired} flag is resolved per request. */
@Component
@RequiredArgsConstructor
public class CachedEventReader {

    private final EventStore<? extends EventView, EventRequestDto> eventStore;

    @Cacheable(cacheNames = CacheNames.EVENT, key = "#locale + '-' + #id")
    @Transactional(readOnly = true)
    public EventDto getById(Long id, String locale) {
        var event = eventStore.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
        return EventDto.from(event, locale);
    }

    /** Detail including every locale of each localizable field (admin editing). Not cached. */
    @Transactional(readOnly = true)
    public EventDto getForEdit(Long id, String locale) {
        var event = eventStore.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
        return EventDto.fromWithTranslations(event, locale);
    }
}
