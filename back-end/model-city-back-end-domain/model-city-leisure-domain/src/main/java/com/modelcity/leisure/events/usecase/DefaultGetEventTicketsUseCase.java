package com.modelcity.leisure.events.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;import com.modelcity.leisure.events.store.model.EventTicketView;
import com.modelcity.leisure.events.store.model.EventView;
import com.modelcity.leisure.events.controller.model.EventRequestDto;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.events.controller.model.TicketDto;
import com.modelcity.leisure.events.store.EventStore;
import com.modelcity.leisure.events.store.EventTicketStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link GetEventTicketsUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetEventTicketsUseCase implements GetEventTicketsUseCase<TicketDto> {

    private static final int PAGE_SIZE = 20;

    private final EventStore<? extends EventView, EventRequestDto> eventStore;
    private final EventTicketStore<? extends EventTicketView> eventTicketStore;

    @Override
    @Transactional(readOnly = true)
    public Page<TicketDto> execute(Long eventId, int page) {
        if (eventStore.findActiveById(eventId).isEmpty()) {
            throw new ResourceNotFoundException("Event", eventId);
        }
        PageRequest pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("purchasedAt").ascending());
        return eventTicketStore.findByEvent(eventId, pageable).map(TicketDto::privilegedView);
    }
}
