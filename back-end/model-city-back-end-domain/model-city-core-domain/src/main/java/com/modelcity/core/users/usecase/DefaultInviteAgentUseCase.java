package com.modelcity.core.users.usecase;

import com.modelcity.core.trails.SystemTrailGenerator;
import com.modelcity.core.users.controller.model.InviteAgentRequestDto;
import com.modelcity.core.users.facade.Auth0ManagementFacade;
import com.modelcity.core.users.repository.model.UserRole;
import com.modelcity.core.users.usecase.mail.InvitationMailService;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/** Default {@link InviteAgentUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@Slf4j
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultInviteAgentUseCase implements InviteAgentUseCase<InviteAgentRequestDto> {

    private static final Map<UserRole, String> ROLE_LABELS = Map.of(
            UserRole.MODEL_CITY_BACKOFFICE,      "Agente Administrativo",
            UserRole.MODEL_CITY_OPERATOR,        "Agente de Campo",
            UserRole.MODEL_CITY_MOBILITY_AGENT,  "Agente de Movilidad"
    );

    private final Auth0ManagementFacade auth0ManagementFacade;
    private final InvitationMailService invitationMailService;
    private final SystemTrailGenerator systemEventGenerator;

    @Value("${auth0.management.backoffice-role-id}")
    private String backofficeRoleId;

    @Value("${auth0.management.operator-role-id}")
    private String operatorRoleId;

    @Value("${auth0.management.mobility-agent-role-id}")
    private String mobilityAgentRoleId;

    @Override
    public void execute(String requestingSub, InviteAgentRequestDto request) {
        UserRole targetRole = UserRole.fromValue(request.getRole());
        String auth0RoleId = resolveAuth0RoleId(targetRole);

        // 1. Create user in Auth0
        String auth0UserId = auth0ManagementFacade.createUser(request.getEmail(), request.getName());

        // 2. Assign role in Auth0
        auth0ManagementFacade.assignRoles(auth0UserId, List.of(auth0RoleId));

        // 3. Generate password change ticket
        String ticketUrl = auth0ManagementFacade.createPasswordChangeTicket(auth0UserId);

        // 4. Send invitation email with the ticket
        invitationMailService.sendInvitation(request.getEmail(), ROLE_LABELS.get(targetRole), ticketUrl);

        systemEventGenerator.agentInvited(requestingSub, request.getEmail(), request.getName(), targetRole.getValue());
        log.info("Agent invitation sent: email={} role={} requestedBy={}", request.getEmail(), targetRole, requestingSub);
    }

    private String resolveAuth0RoleId(UserRole role) {
        return switch (role) {
            case MODEL_CITY_BACKOFFICE     -> backofficeRoleId;
            case MODEL_CITY_OPERATOR       -> operatorRoleId;
            case MODEL_CITY_MOBILITY_AGENT -> mobilityAgentRoleId;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role for invitation: " + role);
        };
    }
}
