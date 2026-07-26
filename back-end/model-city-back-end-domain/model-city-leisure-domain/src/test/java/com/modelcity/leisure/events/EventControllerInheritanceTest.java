package com.modelcity.leisure.events;

import com.modelcity.leisure.events.controller.DefaultEventController;
import com.modelcity.leisure.events.usecase.CreateEventUseCase;
import com.modelcity.leisure.events.usecase.DeleteEventUseCase;
import com.modelcity.leisure.events.usecase.GetEventForEditUseCase;
import com.modelcity.leisure.events.usecase.GetEventUseCase;
import com.modelcity.leisure.events.usecase.GetEventsUseCase;
import com.modelcity.leisure.events.usecase.UpdateEventUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;

/**
 * Proves the request mapping ({@code /events}) declared on the abstract base {@code EventController} is
 * inherited by a concrete subclass, so a default (or city) subclass exposes the endpoint without
 * re-declaring the mapping. {@code GET /events} must resolve to the subclass'{@code getEvents} handler.
 */
class EventControllerInheritanceTest {

    @Test
    void inheritedMappingResolvesToConcreteSubclassHandler() throws Exception {
        DefaultEventController controller = new DefaultEventController(
                mock(GetEventsUseCase.class),
                mock(GetEventUseCase.class),
                mock(GetEventForEditUseCase.class),
                mock(CreateEventUseCase.class),
                mock(UpdateEventUseCase.class),
                mock(DeleteEventUseCase.class));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/events"))
                .andExpect(handler().handlerType(DefaultEventController.class))
                .andExpect(handler().methodName("getEvents"));
    }
}
