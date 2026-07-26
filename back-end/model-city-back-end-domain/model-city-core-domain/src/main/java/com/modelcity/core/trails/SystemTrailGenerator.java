package com.modelcity.core.trails;

import com.modelcity.common.trails.AbstractSystemTrailGenerator;
import com.modelcity.common.trails.OperationType;
import com.modelcity.core.trails.store.CoreSystemTrailStore;
import com.modelcity.core.otp.repository.model.OperationAuthorization;
import com.modelcity.core.users.repository.model.Neighbourhood;
import com.modelcity.core.users.repository.model.User;
import com.modelcity.core.users.repository.model.UserRole;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

/** Records core system trails (users + OTP). One method per event type in the CORE catalogue. */
@Service("coreSystemTrailGenerator")
public class SystemTrailGenerator extends AbstractSystemTrailGenerator {

    private static final String RES_USER = "USER";
    private static final String RES_AGENT_INVITATION = "AGENT_INVITATION";
    private static final String RES_OPERATION_AUTHORIZATION = "OPERATION_AUTHORIZATION";
    private static final String ADMIN_ROLE = UserRole.MODEL_CITY_PLATFORM_ADMIN.getValue();

    public SystemTrailGenerator(CoreSystemTrailStore store, ObjectMapper objectMapper) {
        super(store, objectMapper);
    }

    /** A citizen signed in for the first time (JIT provisioning). */
    public void userRegistered(User user) {
        record("USER_REGISTERED", OperationType.CREATE, user.getId(), roleValue(user),
                neighbourhoodId(user), null, RES_USER, user.getId(),
                userPayload(user));
    }

    /** An existing user record was updated on sign-in. */
    public void userUpdated(User user) {
        record("USER_UPDATED", OperationType.UPDATE, user.getId(), roleValue(user),
                neighbourhoodId(user), null, RES_USER, user.getId(),
                userPayload(user));
    }

    /** An admin permanently removed a user. */
    public void userDeleted(String adminSub, User user) {
        record("USER_DELETED", OperationType.DELETE, adminSub, ADMIN_ROLE,
                neighbourhoodId(user), null, RES_USER, user.getId(),
                payload("userId", user.getId(), "name", user.getName(),
                        "email", user.getEmail(), "role", roleValue(user)));
    }

    /** An admin enabled or disabled a user account. */
    public void userStatusChanged(String adminSub, User user, String previousStatus, String newStatus) {
        record("USER_STATUS_CHANGED", OperationType.UPDATE, adminSub, ADMIN_ROLE,
                neighbourhoodId(user), null, RES_USER, user.getId(),
                payload("userId", user.getId(), "name", user.getName(),
                        "previousStatus", previousStatus, "newStatus", newStatus));
    }

    /** An admin invited a new staff member. */
    public void agentInvited(String adminSub, String email, String name, String role) {
        record("AGENT_INVITED", OperationType.CREATE, adminSub, ADMIN_ROLE,
                null, null, RES_AGENT_INVITATION, email,
                payload("email", email, "name", name, "role", role));
    }

    public void operationAuthorizationCreated(OperationAuthorization auth) {
        record("OPERATION_AUTHORIZATION_CREATED", OperationType.CREATE, auth.getUserId(), null,
                null, null, RES_OPERATION_AUTHORIZATION, auth.getOperationAuthorizationId().toString(),
                authPayload(auth));
    }

    public void operationAuthorizationVerified(OperationAuthorization auth) {
        record("OPERATION_AUTHORIZATION_VERIFIED", OperationType.UPDATE, auth.getUserId(), null,
                null, null, RES_OPERATION_AUTHORIZATION, auth.getOperationAuthorizationId().toString(),
                authPayload(auth));
    }

    public void operationAuthorizationBurnt(OperationAuthorization auth) {
        record("OPERATION_AUTHORIZATION_BURNT", OperationType.UPDATE, auth.getUserId(), null,
                null, null, RES_OPERATION_AUTHORIZATION, auth.getOperationAuthorizationId().toString(),
                authPayload(auth));
    }

    private static Object userPayload(User user) {
        Neighbourhood n = user.getNeighbourhood();
        return payload(
                "userId", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "neighbourhoodId", n == null ? null : n.getId(),
                "neighbourhoodName", n == null ? null : n.getName(),
                "role", roleValue(user));
    }

    private static Object authPayload(OperationAuthorization auth) {
        return payload(
                "operationAuthorizationId", auth.getOperationAuthorizationId().toString(),
                "operationType", auth.getOperationType(),
                "resourceType", auth.getResourceType(),
                "resourceId", auth.getResourceId(),
                "status", auth.getStatus() == null ? null : auth.getStatus().name());
    }

    private static String roleValue(User user) {
        return user.getRole() == null ? null : user.getRole().getValue();
    }

    private static Long neighbourhoodId(User user) {
        return user.getNeighbourhood() == null ? null : user.getNeighbourhood().getId();
    }
}
