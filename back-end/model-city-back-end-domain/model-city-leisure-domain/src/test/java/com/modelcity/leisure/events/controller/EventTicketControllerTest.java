package com.modelcity.leisure.events.controller;

import com.modelcity.leisure.events.controller.model.PurchaseTicketRequestDto;
import com.modelcity.leisure.events.controller.model.RefundDto;
import com.modelcity.leisure.events.controller.model.RefundRequestDto;
import com.modelcity.leisure.events.controller.model.TicketDto;
import com.modelcity.leisure.events.usecase.GetEventTicketsUseCase;
import com.modelcity.leisure.events.usecase.PurchaseTicketUseCase;
import com.modelcity.leisure.events.usecase.RefundTicketUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventTicketControllerTest {

    @Mock PurchaseTicketUseCase<TicketDto, PurchaseTicketRequestDto> purchaseTicketUseCase;
    @Mock GetEventTicketsUseCase<TicketDto> getEventTicketsUseCase;
    @Mock RefundTicketUseCase<RefundDto, RefundRequestDto> refundTicketUseCase;

    DefaultEventTicketController controller;

    @BeforeEach
    void setUp() {
        controller = new DefaultEventTicketController(purchaseTicketUseCase, getEventTicketsUseCase,
                refundTicketUseCase);
    }

    @Test
    void purchaseTicket_delegatesToUseCase() {
        PurchaseTicketRequestDto request = new PurchaseTicketRequestDto();
        controller.purchaseTicket(1L, "citizen-sub", request);
        verify(purchaseTicketUseCase).execute(1L, "citizen-sub", request);
    }

    @Test
    void getTickets_delegatesToUseCase() {
        controller.getTickets(1L, 2);
        verify(getEventTicketsUseCase).execute(1L, 2);
    }

    @Test
    void refundTicket_delegatesToUseCase() {
        RefundRequestDto request = new RefundRequestDto();
        controller.refundTicket(1L, 10L, "sub-agent", request);
        verify(refundTicketUseCase).execute(1L, 10L, "sub-agent", request);
    }

    @Test
    void refundTicket_withoutBody_delegatesWithNull() {
        controller.refundTicket(1L, 10L, "sub-agent", null);
        verify(refundTicketUseCase).execute(1L, 10L, "sub-agent", null);
    }
}
