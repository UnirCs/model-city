package com.modelcity.leisure.events.controller;

import com.modelcity.leisure.events.controller.model.CitizenTicketDto;
import com.modelcity.leisure.events.usecase.GetCitizenTicketsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CitizenTicketControllerTest {

    @Mock GetCitizenTicketsUseCase<CitizenTicketDto> getCitizenTicketsUseCase;

    DefaultCitizenTicketController controller;

    @BeforeEach
    void setUp() {
        controller = new DefaultCitizenTicketController(getCitizenTicketsUseCase);
    }

    @Test
    void getTickets_delegatesToUseCase() {
        controller.getTickets("citizen-sub", "citizen-sub", 1, "upcoming");
        verify(getCitizenTicketsUseCase).execute("citizen-sub", "citizen-sub", 1, "upcoming");
    }

    @Test
    void getTickets_adminQueryingOtherUser_stillDelegates() {
        controller.getTickets("target-sub", "admin-sub", 0, null);
        verify(getCitizenTicketsUseCase).execute("target-sub", "admin-sub", 0, null);
    }
}
