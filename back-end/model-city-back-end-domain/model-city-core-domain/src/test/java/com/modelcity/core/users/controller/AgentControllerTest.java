package com.modelcity.core.users.controller;

import com.modelcity.core.users.controller.model.InviteAgentRequestDto;
import com.modelcity.core.users.usecase.InviteAgentUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AgentControllerTest {

    @Mock InviteAgentUseCase<InviteAgentRequestDto> inviteAgentUseCase;

    DefaultAgentController controller;

    @BeforeEach
    void setUp() {
        controller = new DefaultAgentController(inviteAgentUseCase);
    }

    @Test
    void inviteAgent_delegatesToUseCase() {
        InviteAgentRequestDto request = new InviteAgentRequestDto("Nuevo Agente", "agente@example.com", "MODEL-CITY-BACKOFFICE");
        controller.inviteAgent("admin-sub", request);
        verify(inviteAgentUseCase).execute("admin-sub", request);
    }
}
