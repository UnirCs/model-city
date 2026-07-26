package com.modelcity.common.security;

import com.modelcity.common.client.CoreClient;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModelCityAccessAspectTest {

    @Mock
    CoreClient coreClient;

    @Mock
    JoinPoint joinPoint;

    @Mock
    MethodSignature methodSignature;

    ModelCityAccessAspect aspect;

    static class Target {
        @ModelCityAccess.PlatformAdmin
        void adminOnly() {}

        @ModelCityAccess.PlatformAdmin
        @ModelCityAccess.BackOffice
        void adminOrBackoffice() {}

        @ModelCityAccess.MobilityAgent
        void mobilityAgentOnly() {}
    }

    @BeforeEach
    void setUp() {
        aspect = new ModelCityAccessAspect(coreClient);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    private void mockMethod(String name) throws NoSuchMethodException {
        Method method = Target.class.getDeclaredMethod(name);
        when(methodSignature.getMethod()).thenReturn(method);
    }

    private void mockRequestWithSub(String sub) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (sub != null) {
            request.addHeader(AuthConstants.HEADER_AUTH_SUB, sub);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void checkAccess_matchingRole_allowsCall() throws NoSuchMethodException {
        mockMethod("adminOnly");
        mockRequestWithSub("admin-sub");
        when(coreClient.getUserRole("admin-sub")).thenReturn("MODEL-CITY-PLATFORM-ADMIN");

        assertThatCode(() -> aspect.checkAccess(joinPoint)).doesNotThrowAnyException();
    }

    @Test
    void checkAccess_roleNotAllowed_throwsForbidden() throws NoSuchMethodException {
        mockMethod("adminOnly");
        mockRequestWithSub("citizen-sub");
        when(coreClient.getUserRole("citizen-sub")).thenReturn("MODEL-CITY-CITIZEN");

        assertThatThrownBy(() -> aspect.checkAccess(joinPoint))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Required role");
    }

    @Test
    void checkAccess_missingAuthHeader_throwsForbidden() throws NoSuchMethodException {
        mockMethod("adminOnly");
        mockRequestWithSub(null);

        assertThatThrownBy(() -> aspect.checkAccess(joinPoint))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Missing authentication");
        verifyNoInteractions(coreClient);
    }

    @Test
    void checkAccess_multipleAnnotations_allowsEitherRole() throws NoSuchMethodException {
        mockMethod("adminOrBackoffice");
        mockRequestWithSub("backoffice-sub");
        when(coreClient.getUserRole("backoffice-sub")).thenReturn("MODEL-CITY-BACKOFFICE");

        assertThatCode(() -> aspect.checkAccess(joinPoint)).doesNotThrowAnyException();
    }

    @Test
    void checkAccess_nullRoleFromCore_throwsForbidden() throws NoSuchMethodException {
        mockMethod("mobilityAgentOnly");
        mockRequestWithSub("user-sub");
        when(coreClient.getUserRole("user-sub")).thenReturn(null);

        assertThatThrownBy(() -> aspect.checkAccess(joinPoint))
                .isInstanceOf(ResponseStatusException.class);
    }
}
