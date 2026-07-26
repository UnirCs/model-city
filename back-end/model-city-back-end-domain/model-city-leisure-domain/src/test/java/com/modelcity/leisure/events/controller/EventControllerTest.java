package com.modelcity.leisure.events.controller;

import com.modelcity.leisure.events.controller.model.EventDto;
import com.modelcity.leisure.events.controller.model.EventRequestDto;
import com.modelcity.leisure.events.controller.model.EventSummaryDto;
import com.modelcity.leisure.events.repository.model.EventType;
import com.modelcity.leisure.events.usecase.CreateEventUseCase;
import com.modelcity.leisure.events.usecase.DeleteEventUseCase;
import com.modelcity.leisure.events.usecase.GetEventForEditUseCase;
import com.modelcity.leisure.events.usecase.GetEventUseCase;
import com.modelcity.leisure.events.usecase.GetEventsUseCase;
import com.modelcity.leisure.events.usecase.UpdateEventUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock GetEventsUseCase<EventSummaryDto> getEventsUseCase;
    @Mock GetEventUseCase<EventDto> getEventUseCase;
    @Mock GetEventForEditUseCase<EventDto> getEventForEditUseCase;
    @Mock CreateEventUseCase<EventDto, EventRequestDto> createEventUseCase;
    @Mock UpdateEventUseCase<EventDto, EventRequestDto> updateEventUseCase;
    @Mock DeleteEventUseCase deleteEventUseCase;

    DefaultEventController controller;

    @BeforeEach
    void setUp() {
        controller = new DefaultEventController(getEventsUseCase, getEventUseCase, getEventForEditUseCase,
                createEventUseCase, updateEventUseCase, deleteEventUseCase);
    }

    @Test
    void getEvents_delegatesWithResolvedLocale() {
        controller.getEvents(EventType.MUSIC, true, 1, Locale.FRENCH);
        verify(getEventsUseCase).execute(EventType.MUSIC, true, 1, "fr");
    }

    @Test
    void getEvent_withoutTranslations_usesGetUseCase() {
        controller.getEvent(1L, "citizen-sub", null, Locale.ENGLISH);
        verify(getEventUseCase).execute(1L, "citizen-sub", "en");
        verify(getEventForEditUseCase, never()).execute(any(), any());
    }

    @Test
    void getEvent_withFullTranslations_usesForEditUseCase() {
        controller.getEvent(1L, "citizen-sub", "full", Locale.ENGLISH);
        verify(getEventForEditUseCase).execute(1L, "en");
        verify(getEventUseCase, never()).execute(any(), any(), any());
    }

    @Test
    void createEvent_delegatesToUseCase() {
        EventRequestDto request = new EventRequestDto();
        controller.createEvent("sub-agent", request, Locale.ENGLISH);
        verify(createEventUseCase).execute("sub-agent", request, "en");
    }

    @Test
    void updateEvent_delegatesToUseCase() {
        EventRequestDto request = new EventRequestDto();
        controller.updateEvent(1L, "sub-agent", request, Locale.ENGLISH);
        verify(updateEventUseCase).execute(1L, "sub-agent", request, "en");
    }

    @Test
    void deleteEvent_delegatesToUseCase() {
        controller.deleteEvent(1L, "sub-agent");
        verify(deleteEventUseCase).execute(1L, "sub-agent");
    }
}
