package com.modelcity.core.users.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.core.users.controller.model.InviteAgentRequestDto;

/**
 * Invites a new staff member (BACKOFFICE, OPERATOR or MOBILITY_AGENT) via Auth0 Management API.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultInviteAgentUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface InviteAgentUseCase<R extends InviteAgentRequestDto> {

    void execute(String requestingSub, R request);
}
