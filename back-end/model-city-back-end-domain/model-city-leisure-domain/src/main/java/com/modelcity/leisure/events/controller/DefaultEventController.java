package com.modelcity.leisure.events.controller;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.web.bind.annotation.RestController;
import com.modelcity.leisure.events.controller.model.EventDto;
import com.modelcity.leisure.events.controller.model.EventRequestDto;
import com.modelcity.leisure.events.controller.model.EventSummaryDto;
import com.modelcity.leisure.events.usecase.CreateEventUseCase;
import com.modelcity.leisure.events.usecase.DeleteEventUseCase;
import com.modelcity.leisure.events.usecase.GetEventForEditUseCase;
import com.modelcity.leisure.events.usecase.GetEventUseCase;
import com.modelcity.leisure.events.usecase.GetEventsUseCase;
import com.modelcity.leisure.events.usecase.UpdateEventUseCase;

/**
 * Default concrete {@link EventController}, bound to the platform DTOs. Carries no stereotype annotation of
 * its own, so component scanning never registers it; the domain auto-config registers it as a fallback bean
 * that backs off when a local deployment provides its own {@code EventController} subclass (which may bind its
 * own DTO subtypes).
 */
@RestController
@ModelCityDisabledIfInherited
public class DefaultEventController extends EventController<EventDto, EventSummaryDto, EventRequestDto> {

    public DefaultEventController(
            GetEventsUseCase<EventSummaryDto> getEventsUseCase,
            GetEventUseCase<EventDto> getEventUseCase,
            GetEventForEditUseCase<EventDto> getEventForEditUseCase,
            CreateEventUseCase<EventDto, EventRequestDto> createEventUseCase,
            UpdateEventUseCase<EventDto, EventRequestDto> updateEventUseCase,
            DeleteEventUseCase deleteEventUseCase) {
        super(getEventsUseCase, getEventUseCase, getEventForEditUseCase,
                createEventUseCase, updateEventUseCase, deleteEventUseCase);
    }
}
