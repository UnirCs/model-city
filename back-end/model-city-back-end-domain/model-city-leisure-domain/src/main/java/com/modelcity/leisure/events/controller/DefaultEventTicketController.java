package com.modelcity.leisure.events.controller;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.web.bind.annotation.RestController;
import com.modelcity.leisure.events.controller.model.PurchaseTicketRequestDto;
import com.modelcity.leisure.events.controller.model.RefundDto;
import com.modelcity.leisure.events.controller.model.RefundRequestDto;
import com.modelcity.leisure.events.controller.model.TicketDto;
import com.modelcity.leisure.events.usecase.GetEventTicketsUseCase;
import com.modelcity.leisure.events.usecase.PurchaseTicketUseCase;
import com.modelcity.leisure.events.usecase.RefundTicketUseCase;

/**
 * Default concrete {@link EventTicketController}, bound to the platform DTOs. The component-scanned platform
 * default; disabled at startup when a local deployment provides its own bean for the seam.
 */
@RestController
@ModelCityDisabledIfInherited
public class DefaultEventTicketController
        extends EventTicketController<TicketDto, RefundDto, PurchaseTicketRequestDto, RefundRequestDto> {

    public DefaultEventTicketController(
            PurchaseTicketUseCase<TicketDto, PurchaseTicketRequestDto> purchaseTicketUseCase,
            GetEventTicketsUseCase<TicketDto> getEventTicketsUseCase,
            RefundTicketUseCase<RefundDto, RefundRequestDto> refundTicketUseCase) {
        super(purchaseTicketUseCase, getEventTicketsUseCase, refundTicketUseCase);
    }
}
