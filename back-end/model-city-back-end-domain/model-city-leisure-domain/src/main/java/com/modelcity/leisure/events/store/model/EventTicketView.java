package com.modelcity.leisure.events.store.model;

import com.modelcity.leisure.events.repository.model.TicketStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Read-only view of an event ticket, exposed by the persistence adapter. */
public interface EventTicketView {
    Long getId();
    Long getEventId();
    String getCitizenSub();
    String getCitizenName();
    BigDecimal getPricePaid();
    String getCurrency();
    TicketStatus getStatus();
    LocalDateTime getPurchasedAt();
    LocalDateTime getRefundedAt();
    String getStripeCheckoutSessionId();
}
