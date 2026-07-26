package com.modelcity.leisure.events.controller;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.common.i18n.SupportedLocale;
import com.modelcity.common.security.ModelCityAccess;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

/**
 * Endpoints to browse and manage cultural / leisure events.
 *
 * <p>Extension point: this is the overridable base controller. The platform registers
 * {@link DefaultEventController} as the default bean (see {@code LeisureEventsAutoConfiguration}).
 * A local deployment overrides an endpoint by declaring its own {@code @RestController} that
 * {@code extends EventController} and {@code @Override}s the relevant method (Spring MVC inherits the
 * request mappings); it may also add new endpoints. As soon as such a bean exists, the default backs off.
 *
 * <p>The class is abstract so component scanning never registers it directly; the {@code @RestController}
 * and {@code @RequestMapping} are inherited by the concrete subclasses (default and city ones), which
 * guarantees handler detection and JSON serialization without re-declaring them.
 */
@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@ModelCityExtensionPoint
public abstract class EventController<T extends EventDto, S extends EventSummaryDto, R extends EventRequestDto> {

    protected final GetEventsUseCase<S> getEventsUseCase;
    protected final GetEventUseCase<T> getEventUseCase;
    protected final GetEventForEditUseCase<T> getEventForEditUseCase;
    protected final CreateEventUseCase<T, R> createEventUseCase;
    protected final UpdateEventUseCase<T, R> updateEventUseCase;
    protected final DeleteEventUseCase deleteEventUseCase;

    /** Lists active events with optional type and paid filters (page size 6). */
    @GetMapping
    public Page<S> getEvents(
            @RequestParam(required = false) EventType type,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(defaultValue = "0") int page,
            Locale locale) {
        log.debug("GET /events type={} paid={} page={}", type, paid, page);
        return getEventsUseCase.execute(type, paid, page, SupportedLocale.from(locale).code());
    }

    /** Returns the detail of a single event. {@code ?translations=full} returns every locale (admin editing). */
    @GetMapping("/{id}")
    public T getEvent(
            @PathVariable Long id,
            @RequestHeader(value = "X-Auth-Sub", required = false) String sub,
            @RequestParam(required = false) String translations,
            Locale locale) {
        log.debug("GET /events/{} sub={} translations={}", id, sub, translations);
        String code = SupportedLocale.from(locale).code();
        return "full".equals(translations)
                ? getEventForEditUseCase.execute(id, code)
                : getEventUseCase.execute(id, sub, code);
    }

    /** Creates a new event. Backoffice or admin. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ModelCityAccess.PlatformAdmin
    @ModelCityAccess.BackOffice
    public T createEvent(
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid R request,
            Locale locale) {
        log.debug("POST /events sub={}", sub);
        return createEventUseCase.execute(sub, request, SupportedLocale.from(locale).code());
    }

    /** Updates an existing event. Backoffice or admin. */
    @PutMapping("/{id}")
    @ModelCityAccess.PlatformAdmin
    @ModelCityAccess.BackOffice
    public T updateEvent(
            @PathVariable Long id,
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid R request,
            Locale locale) {
        log.debug("PUT /events/{} sub={}", id, sub);
        return updateEventUseCase.execute(id, sub, request, SupportedLocale.from(locale).code());
    }

    /** Soft-deletes an event and automatically refunds outstanding tickets. Admin only. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ModelCityAccess.PlatformAdmin
    public void deleteEvent(
            @PathVariable Long id,
            @RequestHeader("X-Auth-Sub") String sub) {
        log.debug("DELETE /events/{} sub={}", id, sub);
        deleteEventUseCase.execute(id, sub);
    }
}
