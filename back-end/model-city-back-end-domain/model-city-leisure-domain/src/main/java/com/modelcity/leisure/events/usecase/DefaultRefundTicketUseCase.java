package com.modelcity.leisure.events.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;
import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.events.controller.model.RefundDto;
import com.modelcity.leisure.events.controller.model.RefundRequestDto;
import com.modelcity.leisure.facade.StripeFacade;
import com.modelcity.leisure.events.store.model.EventRefundView;
import com.modelcity.leisure.events.store.model.EventTicketView;
import com.modelcity.leisure.events.repository.model.TicketStatus;
import com.modelcity.leisure.events.store.EventRefundStore;
import com.modelcity.leisure.events.store.EventTicketStore;
import com.modelcity.leisure.trails.SystemTrailGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Default {@link RefundTicketUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultRefundTicketUseCase implements RefundTicketUseCase<RefundDto, RefundRequestDto> {

    private final EventTicketStore<? extends EventTicketView> eventTicketStore;
    private final EventRefundStore<? extends EventRefundView> eventRefundStore;
    private final StripeFacade stripeFacade;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    public RefundDto execute(Long eventId, Long ticketId, String issuedBySub, RefundRequestDto request) {
        EventTicketView ticket = eventTicketStore.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));
        if (!ticket.getEventId().equals(eventId)) {
            throw new ResourceNotFoundException("Ticket", ticketId);
        }
        if (ticket.getStatus() == TicketStatus.REFUNDED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ticket is already refunded");
        }

        if (ticket.getPricePaid().compareTo(BigDecimal.ZERO) > 0 && ticket.getStripeCheckoutSessionId() != null) {
            stripeFacade.refund(
                    ticket.getStripeCheckoutSessionId(), ticket.getPricePaid(), ticket.getCurrency());
        }

        eventTicketStore.markRefunded(ticketId, LocalDateTime.now());

        String reason = request == null ? null : request.getReason();
        EventRefundView saved = eventRefundStore.create(ticketId, ticket.getPricePaid(), ticket.getCurrency(),
                reason, false, issuedBySub);
        systemEventGenerator.eventTicketRefunded(issuedBySub, ticket, saved.getId(), ticket.getPricePaid(), reason);
        log.info("Refund id={} issued for ticket={} by sub={}", saved.getId(), ticketId, issuedBySub);
        return RefundDto.from(saved);
    }
}
