package com.modelcity.leisure.events;

import com.modelcity.common.client.CoreClient;
import com.modelcity.common.config.ModelCityExtensibilityAutoConfiguration;
import com.modelcity.leisure.config.StripeProperties;
import com.modelcity.leisure.events.controller.DefaultCitizenTicketController;
import com.modelcity.leisure.events.controller.DefaultEventController;
import com.modelcity.leisure.events.controller.DefaultEventTicketController;
import com.modelcity.leisure.events.controller.DefaultStripeWebhookController;
import com.modelcity.leisure.events.controller.EventController;
import com.modelcity.leisure.events.controller.model.EventDto;
import com.modelcity.leisure.events.controller.model.EventRequestDto;
import com.modelcity.leisure.events.controller.model.EventSummaryDto;
import com.modelcity.leisure.events.repository.model.EventType;
import com.modelcity.leisure.events.store.EventRefundStore;
import com.modelcity.leisure.events.store.EventStore;
import com.modelcity.leisure.events.store.EventTicketStore;
import com.modelcity.leisure.events.usecase.CachedEventReader;
import com.modelcity.leisure.events.usecase.CreateEventUseCase;
import com.modelcity.leisure.events.usecase.DefaultCreateEventUseCase;
import com.modelcity.leisure.events.usecase.DefaultDeleteEventUseCase;
import com.modelcity.leisure.events.usecase.DefaultGetCitizenTicketsUseCase;
import com.modelcity.leisure.events.usecase.DefaultGetEventForEditUseCase;
import com.modelcity.leisure.events.usecase.DefaultGetEventTicketsUseCase;
import com.modelcity.leisure.events.usecase.DefaultGetEventUseCase;
import com.modelcity.leisure.events.usecase.DefaultGetEventsUseCase;
import com.modelcity.leisure.events.usecase.DefaultPurchaseTicketUseCase;
import com.modelcity.leisure.events.usecase.DefaultRefundTicketUseCase;
import com.modelcity.leisure.events.usecase.DefaultUpdateEventUseCase;
import com.modelcity.leisure.events.usecase.DeleteEventUseCase;
import com.modelcity.leisure.events.usecase.EventWriteValidator;
import com.modelcity.leisure.events.usecase.GetEventForEditUseCase;
import com.modelcity.leisure.events.usecase.GetEventUseCase;
import com.modelcity.leisure.events.usecase.GetEventsUseCase;
import com.modelcity.leisure.events.usecase.UpdateEventUseCase;
import com.modelcity.leisure.facade.StripeFacade;
import com.modelcity.leisure.trails.SystemTrailGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Verifies the events vertical extension points under the scanned-defaults model: the {@code Default*} beans
 * carry a regular stereotype plus {@code @ModelCityDisabledIfInherited} and are registered by component
 * scanning (here simulated with {@code withBean}). By default the platform's {@code Default*} beans are wired,
 * and as soon as a (simulated) local deployment declares its own use case or controller bean for the same
 * seam, the matching default is removed at startup by
 * {@link com.modelcity.common.extensibility.ModelCityDisabledIfInheritedProcessor} (registered by
 * {@link ModelCityExtensibilityAutoConfiguration}).
 */
class EventExtensionPointsTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ModelCityExtensibilityAutoConfiguration.class))
            .withBean(EventStore.class, () -> mock(EventStore.class))
            .withBean(EventTicketStore.class, () -> mock(EventTicketStore.class))
            .withBean(EventRefundStore.class, () -> mock(EventRefundStore.class))
            .withBean(CachedEventReader.class, () -> mock(CachedEventReader.class))
            .withBean(EventWriteValidator.class, () -> mock(EventWriteValidator.class))
            .withBean(StripeFacade.class, () -> mock(StripeFacade.class))
            .withBean(SystemTrailGenerator.class, () -> mock(SystemTrailGenerator.class))
            .withBean(CoreClient.class, () -> mock(CoreClient.class))
            .withBean(StripeProperties.class, () -> mock(StripeProperties.class))
            // events
            .withBean(DefaultGetEventsUseCase.class)
            .withBean(DefaultGetEventUseCase.class)
            .withBean(DefaultGetEventForEditUseCase.class)
            .withBean(DefaultCreateEventUseCase.class)
            .withBean(DefaultUpdateEventUseCase.class)
            .withBean(DefaultDeleteEventUseCase.class)
            .withBean(DefaultEventController.class)
            // event tickets
            .withBean(DefaultGetEventTicketsUseCase.class)
            .withBean(DefaultGetCitizenTicketsUseCase.class)
            .withBean(DefaultPurchaseTicketUseCase.class)
            .withBean(DefaultRefundTicketUseCase.class)
            .withBean(DefaultEventTicketController.class)
            .withBean(DefaultCitizenTicketController.class)
            .withBean(DefaultStripeWebhookController.class);

    @Test
    void wiresPlatformDefaultsWhenNoOverrideIsPresent() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(GetEventsUseCase.class);
            assertThat(context).hasSingleBean(EventController.class);
            assertThat(context.getBean(GetEventsUseCase.class)).isInstanceOf(DefaultGetEventsUseCase.class);
            assertThat(context.getBean(EventController.class)).isInstanceOf(DefaultEventController.class);
        });
    }

    @Test
    void cityUseCaseOverrideWinsAndDefaultBacksOff() {
        runner.withUserConfiguration(CityUseCaseConfig.class).run(context -> {
            assertThat(context).hasSingleBean(GetEventsUseCase.class);
            assertThat(context).doesNotHaveBean(DefaultGetEventsUseCase.class);
            assertThat(context.getBean(GetEventsUseCase.class)).isInstanceOf(CityGetEventsUseCase.class);
        });
    }

    @Test
    void cityControllerOverrideWinsAndDefaultBacksOff() {
        runner.withUserConfiguration(CityControllerConfig.class).run(context -> {
            assertThat(context).hasSingleBean(EventController.class);
            assertThat(context).doesNotHaveBean(DefaultEventController.class);
            assertThat(context.getBean(EventController.class)).isInstanceOf(CityEventController.class);
        });
    }

    @Configuration
    static class CityUseCaseConfig {
        @Bean
        GetEventsUseCase<EventSummaryDto> cityGetEventsUseCase() {
            return new CityGetEventsUseCase();
        }
    }

    /** Stand-in for a local deployment's own use case implementation. */
    static class CityGetEventsUseCase implements GetEventsUseCase<EventSummaryDto> {
        @Override
        public Page<EventSummaryDto> execute(EventType type, Boolean paid, int page, String locale) {
            return Page.empty();
        }
    }

    @Configuration
    static class CityControllerConfig {
        @Bean
        EventController<EventDto, EventSummaryDto, EventRequestDto> cityEventController(
                GetEventsUseCase<EventSummaryDto> getEventsUseCase,
                GetEventUseCase<EventDto> getEventUseCase,
                GetEventForEditUseCase<EventDto> getEventForEditUseCase,
                CreateEventUseCase<EventDto, EventRequestDto> createEventUseCase,
                UpdateEventUseCase<EventDto, EventRequestDto> updateEventUseCase,
                DeleteEventUseCase deleteEventUseCase) {
            return new CityEventController(getEventsUseCase, getEventUseCase, getEventForEditUseCase,
                    createEventUseCase, updateEventUseCase, deleteEventUseCase);
        }
    }

    /** Stand-in for a local deployment extending the base controller and overriding an endpoint. */
    static class CityEventController extends EventController<EventDto, EventSummaryDto, EventRequestDto> {
        CityEventController(
                GetEventsUseCase<EventSummaryDto> getEventsUseCase,
                GetEventUseCase<EventDto> getEventUseCase,
                GetEventForEditUseCase<EventDto> getEventForEditUseCase,
                CreateEventUseCase<EventDto, EventRequestDto> createEventUseCase,
                UpdateEventUseCase<EventDto, EventRequestDto> updateEventUseCase,
                DeleteEventUseCase deleteEventUseCase) {
            super(getEventsUseCase, getEventUseCase, getEventForEditUseCase,
                    createEventUseCase, updateEventUseCase, deleteEventUseCase);
        }

        @Override
        public Page<EventSummaryDto> getEvents(EventType type, Boolean paid, int page, java.util.Locale locale) {
            return Page.empty();
        }
    }
}
