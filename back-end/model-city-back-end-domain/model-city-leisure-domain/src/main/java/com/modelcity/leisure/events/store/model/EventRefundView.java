package com.modelcity.leisure.events.store.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Read-only view of an event refund, exposed by the persistence adapter. */
public interface EventRefundView {
    Long getId();
    Long getTicketId();
    BigDecimal getAmount();
    String getCurrency();
    String getReason();
    boolean isAutomatic();
    String getIssuedBySub();
    LocalDateTime getRefundedAt();
}
