package com.modelcity.leisure.events.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.events.controller.model.RefundDto;
import com.modelcity.leisure.events.controller.model.RefundRequestDto;

/**
 * Issues a manual refund for a ticket. Backoffice or admin.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultRefundTicketUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface RefundTicketUseCase<T extends RefundDto, R extends RefundRequestDto> {

    T execute(Long eventId, Long ticketId, String issuedBySub, R request);
}
