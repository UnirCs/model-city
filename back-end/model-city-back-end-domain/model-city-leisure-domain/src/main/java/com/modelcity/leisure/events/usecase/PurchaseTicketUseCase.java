package com.modelcity.leisure.events.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.events.controller.model.PurchaseTicketRequestDto;
import com.modelcity.leisure.events.controller.model.TicketDto;

/**
 * Creates a pending ticket for the web checkout flow. Confirmed by Stripe webhook.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultPurchaseTicketUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface PurchaseTicketUseCase<T extends TicketDto, R extends PurchaseTicketRequestDto> {

    T execute(Long eventId, String citizenSub, R request);
}
