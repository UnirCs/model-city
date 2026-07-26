package com.modelcity.leisure.events.controller;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.common.security.ModelCityAccess;
import com.modelcity.leisure.events.controller.model.PurchaseTicketRequestDto;
import com.modelcity.leisure.events.controller.model.RefundDto;
import com.modelcity.leisure.events.controller.model.RefundRequestDto;
import com.modelcity.leisure.events.controller.model.TicketDto;
import com.modelcity.leisure.events.usecase.GetEventTicketsUseCase;
import com.modelcity.leisure.events.usecase.PurchaseTicketUseCase;
import com.modelcity.leisure.events.usecase.RefundTicketUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Ticket and refund endpoints scoped to a specific event.
 *
 * <p>Overridable base controller (abstract): the platform registers {@link DefaultEventTicketController} as
 * the default bean. A local deployment overrides by declaring its own {@code @RestController} that
 * {@code extends EventTicketController}; the default then backs off.
 */
@Slf4j
@RestController
@RequestMapping("/events/{eventId}/tickets")
@RequiredArgsConstructor
@ModelCityExtensionPoint
public abstract class EventTicketController<T extends TicketDto, R extends RefundDto,
        P extends PurchaseTicketRequestDto, Q extends RefundRequestDto> {

    protected final PurchaseTicketUseCase<T, P> purchaseTicketUseCase;
    protected final GetEventTicketsUseCase<T> getEventTicketsUseCase;
    protected final RefundTicketUseCase<R, Q> refundTicketUseCase;

    /** Purchases a ticket as a citizen (web checkout flow). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public T purchaseTicket(
            @PathVariable Long eventId,
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid P request) {
        log.debug("POST /events/{}/tickets sub={}", eventId, sub);
        return purchaseTicketUseCase.execute(eventId, sub, request);
    }

    /** Lists tickets for an event (paginated). Backoffice or admin. */
    @GetMapping
    @ModelCityAccess.PlatformAdmin
    @ModelCityAccess.BackOffice
    public Page<T> getTickets(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page) {
        log.debug("GET /events/{}/tickets page={}", eventId, page);
        return getEventTicketsUseCase.execute(eventId, page);
    }

    /** Issues a refund for the given ticket. Backoffice or admin. */
    @PostMapping("/{ticketId}/refunds")
    @ResponseStatus(HttpStatus.CREATED)
    @ModelCityAccess.PlatformAdmin
    @ModelCityAccess.BackOffice
    public R refundTicket(
            @PathVariable Long eventId,
            @PathVariable Long ticketId,
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody(required = false) @Valid Q request) {
        log.debug("POST /events/{}/tickets/{}/refunds sub={}", eventId, ticketId, sub);
        return refundTicketUseCase.execute(eventId, ticketId, sub, request);
    }
}
