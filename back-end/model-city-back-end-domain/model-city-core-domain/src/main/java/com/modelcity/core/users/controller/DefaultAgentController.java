package com.modelcity.core.users.controller;

import com.modelcity.core.users.controller.model.InviteAgentRequestDto;
import com.modelcity.core.users.usecase.InviteAgentUseCase;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.web.bind.annotation.RestController;

/**
 * Default concrete {@link AgentController}. The component-scanned platform default; disabled at startup when a
 * local deployment provides its own bean for the seam.
 */
@RestController
@ModelCityDisabledIfInherited
public class DefaultAgentController extends AgentController<InviteAgentRequestDto> {

    public DefaultAgentController(InviteAgentUseCase<InviteAgentRequestDto> inviteAgentUseCase) {
        super(inviteAgentUseCase);
    }
}
