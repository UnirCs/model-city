package com.modelcity.leisure.events.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;
import com.modelcity.common.i18n.LocalizedText;
import com.modelcity.leisure.events.controller.model.EventDto;
import com.modelcity.leisure.events.controller.model.EventRequestDto;
import com.modelcity.leisure.facade.StripeFacade;
import com.modelcity.leisure.events.store.model.EventView;
import com.modelcity.leisure.events.store.EventStore;
import com.modelcity.leisure.trails.SystemTrailGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link CreateEventUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultCreateEventUseCase implements CreateEventUseCase<EventDto, EventRequestDto> {

    private final EventStore<? extends EventView, EventRequestDto> eventStore;
    private final EventWriteValidator validator;
    private final StripeFacade stripeFacade;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    @CacheEvict(cacheNames = CacheNames.EVENTS, allEntries = true)
    public EventDto execute(String sub, EventRequestDto request, String locale) {
        validator.validate(request);
        String defaultName = LocalizedText.requireDefault("name", request.getName());
        String stripePriceId = request.isPaid()
                ? stripeFacade.createPrice(defaultName, request.getPrice(), request.getCurrency().toUpperCase())
                : null;
        EventView saved = eventStore.create(request, stripePriceId);
        systemEventGenerator.eventCreated(sub, saved);
        log.info("Event created id={} stripePriceId={} by sub={}", saved.getId(), saved.getStripePriceId(), sub);
        return EventDto.from(saved, locale);
    }
}
