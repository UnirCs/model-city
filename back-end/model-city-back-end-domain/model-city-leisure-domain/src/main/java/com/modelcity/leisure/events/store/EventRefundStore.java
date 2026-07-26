package com.modelcity.leisure.events.store;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;

import com.modelcity.leisure.events.store.model.EventRefundView;

import java.math.BigDecimal;

/** Persistence port for event refunds. */
@ModelCityExtensionPoint
public interface EventRefundStore<T extends EventRefundView> {

    T create(Long ticketId, BigDecimal amount, String currency,
                           String reason, boolean automatic, String issuedBySub);
}
