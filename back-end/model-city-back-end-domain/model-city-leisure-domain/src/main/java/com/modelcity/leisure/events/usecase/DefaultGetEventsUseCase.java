package com.modelcity.leisure.events.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;import com.modelcity.leisure.events.store.model.EventView;
import com.modelcity.leisure.events.controller.model.EventRequestDto;

import com.modelcity.leisure.events.controller.model.EventSummaryDto;
import com.modelcity.leisure.events.repository.model.EventType;
import com.modelcity.leisure.events.store.EventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Default {@link GetEventsUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetEventsUseCase implements GetEventsUseCase<EventSummaryDto> {

    private static final int PAGE_SIZE = 6;

    private final EventStore<? extends EventView, EventRequestDto> eventStore;

    @Override
    @Cacheable(cacheNames = CacheNames.EVENTS, key = "#locale + '-' + #type + '-' + #paid + '-' + #page")
    @Transactional(readOnly = true)
    public Page<EventSummaryDto> execute(EventType type, Boolean paid, int page, String locale) {
        PageRequest pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("startsAt").ascending());
        return eventStore.search(type, paid, LocalDateTime.now(), pageable).map(e -> EventSummaryDto.from(e, locale));
    }
}
