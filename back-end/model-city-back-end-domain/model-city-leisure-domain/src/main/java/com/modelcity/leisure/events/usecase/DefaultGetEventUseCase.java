package com.modelcity.leisure.events.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;import com.modelcity.leisure.events.store.model.EventTicketView;

import com.modelcity.leisure.events.controller.model.EventDto;
import com.modelcity.leisure.events.repository.model.TicketStatus;
import com.modelcity.leisure.events.store.EventTicketStore;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Default {@link GetEventUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetEventUseCase implements GetEventUseCase<EventDto> {

    private final CachedEventReader cachedEventReader;
    private final EventTicketStore<? extends EventTicketView> eventTicketStore;

    @Override
    @Transactional(readOnly = true)
    public EventDto execute(Long id, String sub, String locale) {
        EventDto event = cachedEventReader.getById(id, locale);
        boolean acquired = sub != null && eventTicketStore.existsByEventAndCitizenAndStatusIn(
                id, sub, List.of(TicketStatus.PAID, TicketStatus.PURCHASED));
        return event.withAcquired(acquired);
    }
}
