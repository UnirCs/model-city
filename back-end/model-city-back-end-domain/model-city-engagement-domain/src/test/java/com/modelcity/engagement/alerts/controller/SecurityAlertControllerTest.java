package com.modelcity.engagement.alerts.controller;

import com.modelcity.engagement.alerts.controller.model.SecurityAlertDto;
import com.modelcity.engagement.alerts.controller.model.SecurityAlertRequestDto;
import com.modelcity.engagement.alerts.usecase.CreateSecurityAlertUseCase;
import com.modelcity.engagement.alerts.usecase.DeleteSecurityAlertUseCase;
import com.modelcity.engagement.alerts.usecase.GetSecurityAlertsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SecurityAlertControllerTest {

    @Mock GetSecurityAlertsUseCase<SecurityAlertDto> getSecurityAlertsUseCase;
    @Mock CreateSecurityAlertUseCase<SecurityAlertDto, SecurityAlertRequestDto> createSecurityAlertUseCase;
    @Mock DeleteSecurityAlertUseCase deleteSecurityAlertUseCase;

    DefaultSecurityAlertController controller;

    @BeforeEach
    void setUp() {
        controller = new DefaultSecurityAlertController(getSecurityAlertsUseCase, createSecurityAlertUseCase,
                deleteSecurityAlertUseCase);
    }

    @Test
    void getSecurityAlerts_delegatesWithResolvedLocale() {
        controller.getSecurityAlerts(1, Locale.FRENCH);
        verify(getSecurityAlertsUseCase).execute(1, "fr");
    }

    @Test
    void createSecurityAlert_delegatesToUseCase() {
        SecurityAlertRequestDto request = new SecurityAlertRequestDto();
        controller.createSecurityAlert("agent-sub", request, Locale.ENGLISH);
        verify(createSecurityAlertUseCase).execute("agent-sub", request, "en");
    }

    @Test
    void deleteSecurityAlert_delegatesToUseCase() {
        controller.deleteSecurityAlert(1L, "agent-sub");
        verify(deleteSecurityAlertUseCase).execute(1L, "agent-sub");
    }
}
