package com.modelcity.leisure.events.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;import com.modelcity.leisure.events.store.model.EventRefundView;
import com.modelcity.leisure.events.controller.model.EventRequestDto;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.events.store.model.EventTicketView;
import com.modelcity.leisure.events.store.model.EventView;
import com.modelcity.leisure.events.repository.model.TicketStatus;
import com.modelcity.leisure.events.store.EventRefundStore;
import com.modelcity.leisure.events.store.EventStore;
import com.modelcity.leisure.events.store.EventTicketStore;
import com.modelcity.leisure.trails.SystemTrailGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Default {@link DeleteEventUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultDeleteEventUseCase implements DeleteEventUseCase {

    private static final String AUTO_REASON = "Automatic refund: event cancelled by administrator";

    private final EventStore<? extends EventView, EventRequestDto> eventStore;
    private final EventTicketStore<? extends EventTicketView> eventTicketStore;
    private final EventRefundStore<? extends EventRefundView> eventRefundStore;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.EVENT, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.EVENTS, allEntries = true)
    })
    public void execute(Long id, String sub) {
        EventView event = eventStore.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));

        List<? extends EventTicketView> outstanding = eventTicketStore.findByEventAndStatus(id, TicketStatus.PURCHASED);
        LocalDateTime now = LocalDateTime.now();
        BigDecimal totalRefunded = BigDecimal.ZERO;
        for (EventTicketView ticket : outstanding) {
            eventTicketStore.markRefunded(ticket.getId(), now);
            eventRefundStore.create(ticket.getId(), ticket.getPricePaid(), ticket.getCurrency(),
                    AUTO_REASON, true, sub);
            systemEventGenerator.eventTicketAutoRefunded(sub, ticket, ticket.getPricePaid(), AUTO_REASON);
            if (ticket.getPricePaid() != null) {
                totalRefunded = totalRefunded.add(ticket.getPricePaid());
            }
        }

        eventStore.softDelete(id);
        systemEventGenerator.eventDeleted(sub, event, outstanding.size(), totalRefunded);
        log.info("Event id={} soft-deleted by sub={}; auto-refunded {} ticket(s)", id, sub, outstanding.size());
    }
}
