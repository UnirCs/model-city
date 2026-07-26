package com.modelcity.leisure.events.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;
import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.events.controller.model.EventDto;
import com.modelcity.leisure.events.controller.model.EventRequestDto;
import com.modelcity.leisure.events.store.model.EventView;
import com.modelcity.leisure.events.store.EventStore;
import com.modelcity.leisure.trails.SystemTrailGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link UpdateEventUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultUpdateEventUseCase implements UpdateEventUseCase<EventDto, EventRequestDto> {

    private final EventStore<? extends EventView, EventRequestDto> eventStore;
    private final EventWriteValidator validator;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.EVENT, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.EVENTS, allEntries = true)
    })
    public EventDto execute(Long id, String sub, EventRequestDto request, String locale) {
        if (eventStore.findActiveById(id).isEmpty()) {
            throw new ResourceNotFoundException("Event", id);
        }
        validator.validate(request);
        EventView saved = eventStore.update(id, request);
        systemEventGenerator.eventUpdated(sub, saved);
        log.info("Event id={} updated by sub={}", id, sub);
        return EventDto.from(saved, locale);
    }
}
