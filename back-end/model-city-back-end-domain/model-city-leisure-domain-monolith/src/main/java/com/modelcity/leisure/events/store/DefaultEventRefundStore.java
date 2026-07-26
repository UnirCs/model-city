package com.modelcity.leisure.events.store;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Component;
import com.modelcity.leisure.events.store.model.EventRefundView;
import com.modelcity.leisure.events.repository.EventRefundRepository;
import com.modelcity.leisure.events.repository.model.EventRefund;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/** JPA adapter for the event refund persistence port. */
@RequiredArgsConstructor
@Component
@ModelCityDisabledIfInherited
public class DefaultEventRefundStore implements EventRefundStore<EventRefund> {

    private final EventRefundRepository<EventRefund> eventRefundRepository;

    @Override
    public EventRefund create(Long ticketId, BigDecimal amount, String currency,
                                  String reason, boolean automatic, String issuedBySub) {
        EventRefund refund = EventRefund.builder()
                .ticketId(ticketId)
                .amount(amount)
                .currency(currency)
                .reason(reason)
                .automatic(automatic)
                .issuedBySub(issuedBySub)
                .build();
        return eventRefundRepository.save(refund);
    }
}
