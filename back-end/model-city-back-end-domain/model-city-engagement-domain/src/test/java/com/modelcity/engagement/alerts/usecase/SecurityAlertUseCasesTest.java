package com.modelcity.engagement.alerts.usecase;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.engagement.alerts.controller.model.SecurityAlertDto;
import com.modelcity.engagement.alerts.controller.model.SecurityAlertRequestDto;
import com.modelcity.engagement.alerts.repository.model.AlertSeverity;
import com.modelcity.engagement.alerts.store.SecurityAlertStore;
import com.modelcity.engagement.alerts.store.model.SecurityAlertView;
import com.modelcity.engagement.trails.SystemTrailGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SecurityAlertUseCasesTest {

    @Mock
    @SuppressWarnings("unchecked")
    SecurityAlertStore<SecurityAlertView, SecurityAlertRequestDto> securityAlertStore;

    @Mock
    SystemTrailGenerator systemTrailGenerator;

    private SecurityAlertView mockAlertView(Long id, String title) {
        SecurityAlertView view = mock(SecurityAlertView.class);
        when(view.getId()).thenReturn(id);
        when(view.getTitle()).thenReturn(title);
        when(view.getSeverity()).thenReturn(AlertSeverity.MEDIUM);
        when(view.getDescription()).thenReturn("Descripción del aviso");
        when(view.getLatitude()).thenReturn(40.4);
        when(view.getLongitude()).thenReturn(-3.7);
        when(view.getZoneId()).thenReturn(1L);
        when(view.getCreatedAt()).thenReturn(OffsetDateTime.now());
        when(view.getExpiresAt()).thenReturn(OffsetDateTime.now().plusHours(24));
        when(view.getTranslations()).thenReturn(Map.of());
        return view;
    }

    private SecurityAlertRequestDto buildRequest() {
        SecurityAlertRequestDto req = new SecurityAlertRequestDto();
        req.setTitle(Map.of("es", "Alerta de seguridad"));
        req.setSeverity(AlertSeverity.MEDIUM);
        req.setDescription(Map.of("es", "Descripción del aviso"));
        req.setLatitude(40.4);
        req.setLongitude(-3.7);
        req.setZoneId(1L);
        req.setExpiresAt(OffsetDateTime.now().plusHours(24));
        return req;
    }

    @Nested
    class CreateSecurityAlertTests {

        DefaultCreateSecurityAlertUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultCreateSecurityAlertUseCase(securityAlertStore, systemTrailGenerator);
        }

        @Test
        void execute_createsAlertWithFutureExpiry() {
            SecurityAlertRequestDto request = buildRequest();
            SecurityAlertView saved = mockAlertView(1L, "Alerta de seguridad");

            when(securityAlertStore.create(request)).thenReturn(saved);

            SecurityAlertDto result = useCase.execute("agent-sub", request, "es");

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Alerta de seguridad");
            verify(systemTrailGenerator).securityAlertCreated("agent-sub", saved);
        }

        @Test
        void execute_throwsBadRequestWhenExpiryInPast() {
            SecurityAlertRequestDto request = buildRequest();
            request.setExpiresAt(OffsetDateTime.now().minusHours(1));

            assertThatThrownBy(() -> useCase.execute("agent-sub", request, "es"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("expiresAt must be in the future");
        }
    }

    @Nested
    class DeleteSecurityAlertTests {

        DefaultDeleteSecurityAlertUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultDeleteSecurityAlertUseCase(securityAlertStore, systemTrailGenerator);
        }

        @Test
        void execute_deletesExistingAlert() {
            SecurityAlertView view = mockAlertView(1L, "Alerta");
            when(securityAlertStore.findById(1L)).thenReturn(Optional.of(view));

            useCase.execute(1L, "agent-sub");

            verify(securityAlertStore).deleteById(1L);
            verify(systemTrailGenerator).securityAlertDeleted("agent-sub", view);
        }

        @Test
        void execute_throwsWhenNotFound() {
            when(securityAlertStore.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(99L, "agent-sub"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class GetSecurityAlertsTests {

        DefaultGetSecurityAlertsUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetSecurityAlertsUseCase(securityAlertStore);
        }

        @Test
        @SuppressWarnings("unchecked")
        void execute_returnsActiveAlerts() {
            SecurityAlertView view = mockAlertView(1L, "Alerta activa");
            Page<SecurityAlertView> page = new PageImpl<>(List.of(view));
            doReturn(page).when(securityAlertStore).findActive(any(OffsetDateTime.class), any(Pageable.class));

            Page<SecurityAlertDto> result = useCase.execute(0, "es");

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("Alerta activa");
        }

        @Test
        @SuppressWarnings("unchecked")
        void execute_returnsEmptyWhenNoActiveAlerts() {
            Page<SecurityAlertView> emptyPage = new PageImpl<>(List.of());
            doReturn(emptyPage).when(securityAlertStore).findActive(any(OffsetDateTime.class), any(Pageable.class));

            Page<SecurityAlertDto> result = useCase.execute(0, "es");

            assertThat(result.getTotalElements()).isZero();
        }
    }
}
