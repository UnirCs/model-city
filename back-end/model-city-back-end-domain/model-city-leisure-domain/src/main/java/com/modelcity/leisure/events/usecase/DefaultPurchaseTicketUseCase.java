package com.modelcity.leisure.events.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;import com.modelcity.leisure.events.controller.model.EventRequestDto;

import com.modelcity.common.client.CoreClient;
import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.events.controller.model.PurchaseTicketRequestDto;
import com.modelcity.leisure.events.controller.model.TicketDto;
import com.modelcity.leisure.events.store.model.EventTicketView;
import com.modelcity.leisure.events.store.model.EventView;
import com.modelcity.leisure.events.repository.model.TicketStatus;
import com.modelcity.leisure.events.store.EventStore;
import com.modelcity.leisure.events.store.EventTicketStore;
import com.modelcity.leisure.trails.SystemTrailGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** Default {@link PurchaseTicketUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultPurchaseTicketUseCase implements PurchaseTicketUseCase<TicketDto, PurchaseTicketRequestDto> {

    private final EventStore<? extends EventView, EventRequestDto> eventStore;
    private final EventTicketStore<? extends EventTicketView> eventTicketStore;
    private final CoreClient coreClient;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    public TicketDto execute(Long eventId, String citizenSub, PurchaseTicketRequestDto request) {
        EventView event = eventStore.findActiveById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));
        if (!event.isRequiresTicket()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event does not require a ticket");
        }
        if (event.getCapacity() != null) {
            long active = eventTicketStore.countByEventAndStatusIn(eventId,
                    List.of(TicketStatus.PENDING, TicketStatus.PAID, TicketStatus.PURCHASED));
            if (active >= event.getCapacity()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Event is sold out");
            }
        }

        boolean isFree = event.getPrice() == null || event.getPrice().compareTo(java.math.BigDecimal.ZERO) == 0;
        boolean hasSessionId = request.getCheckoutSessionId() != null && !request.getCheckoutSessionId().isBlank();

        if (!isFree && !hasSessionId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "checkoutSessionId is required for paid events");
        }

        EventTicketView saved = eventTicketStore.createPending(
                eventId, citizenSub, safeFetchName(citizenSub),
                event.getPrice(), event.getCurrency(),
                request.getCheckoutSessionId());
        systemEventGenerator.eventTicketPurchased(saved);

        if (isFree) {
            eventTicketStore.markStatus(saved.getId(), TicketStatus.PURCHASED);
            log.info("Free ticket id={} purchased for event={} by sub={}",
                    saved.getId(), eventId, citizenSub);
            return TicketDto.privilegedView(eventTicketStore.findById(saved.getId()).orElseThrow());
        }

        log.info("Pending ticket id={} created for event={} cs={} by sub={}",
                saved.getId(), eventId, request.getCheckoutSessionId(), citizenSub);
        return TicketDto.privilegedView(saved);
    }

    private String safeFetchName(String sub) {
        try {
            return coreClient.getUserName(sub);
        } catch (Exception ex) {
            log.warn("Could not resolve citizen name for sub={}: {}", sub, ex.getMessage());
            return null;
        }
    }
}
