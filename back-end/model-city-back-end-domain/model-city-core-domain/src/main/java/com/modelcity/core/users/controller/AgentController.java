package com.modelcity.core.users.controller;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.common.security.ModelCityAccess;
import com.modelcity.core.users.controller.model.InviteAgentRequestDto;
import com.modelcity.core.users.usecase.InviteAgentUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent invitation endpoints.
 *
 * <p>Overridable base controller (abstract): the platform registers {@link DefaultAgentController} as the
 * default bean. A local deployment overrides by declaring its own {@code @RestController} that
 * {@code extends AgentController}; the default then backs off.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/agents")
@ModelCityExtensionPoint
public abstract class AgentController<R extends InviteAgentRequestDto> {

    protected final InviteAgentUseCase<R> inviteAgentUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ModelCityAccess.PlatformAdmin
    public void inviteAgent(
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid R request) {
        log.debug("POST /agents sub={} email={} role={}", sub, request.getEmail(), request.getRole());
        inviteAgentUseCase.execute(sub, request);
    }
}



