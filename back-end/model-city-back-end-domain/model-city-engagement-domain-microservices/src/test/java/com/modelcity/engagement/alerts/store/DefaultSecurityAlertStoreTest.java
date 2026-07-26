package com.modelcity.engagement.alerts.store;

import com.modelcity.engagement.alerts.controller.model.SecurityAlertRequestDto;
import com.modelcity.engagement.alerts.repository.SecurityAlertRepository;
import com.modelcity.engagement.alerts.repository.model.AlertSeverity;
import com.modelcity.engagement.alerts.repository.model.SecurityAlert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultSecurityAlertStoreTest {

    @Mock
    SecurityAlertRepository<SecurityAlert> securityAlertRepository;

    DefaultSecurityAlertStore store;

    @BeforeEach
    void setUp() {
        store = new DefaultSecurityAlertStore(securityAlertRepository);
    }

    private SecurityAlertRequestDto buildRequest() {
        SecurityAlertRequestDto request = new SecurityAlertRequestDto();
        request.setTitle(Map.of("es", "Alerta", "en", "Alert"));
        request.setSeverity(AlertSeverity.MEDIUM);
        request.setDescription(Map.of("es", "Descripción"));
        request.setLatitude(40.4);
        request.setLongitude(-3.7);
        request.setZoneId(1L);
        request.setNeighbourhoodId(5L);
        request.setExpiresAt(OffsetDateTime.now().plusHours(24));
        return request;
    }

    @Test
    void create_appliesFieldsAndTranslations() {
        when(securityAlertRepository.save(any(SecurityAlert.class))).thenAnswer(inv -> inv.getArgument(0));

        SecurityAlert result = store.create(buildRequest());

        assertThat(result.getTitle()).isEqualTo("Alerta");
        assertThat(result.getSeverity()).isEqualTo(AlertSeverity.MEDIUM);
        assertThat(result.getZoneId()).isEqualTo(1L);
        assertThat(result.getNeighbourhoodId()).isEqualTo(5L);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getTranslations()).containsKey("en");
        assertThat(result.getTranslations().get("en").getTitle()).isEqualTo("Alert");
    }

    @Test
    void findActive_delegatesToRepository() {
        OffsetDateTime now = OffsetDateTime.now();
        store.findActive(now, null);
        verify(securityAlertRepository).findByExpiresAtAfterOrderBySeverityAscCreatedAtDesc(now, null);
    }

    @Test
    void findById_delegatesToRepository() {
        store.findById(1L);
        verify(securityAlertRepository).findById(1L);
    }

    @Test
    void existsById_delegatesToRepository() {
        when(securityAlertRepository.existsById(1L)).thenReturn(true);
        assertThat(store.existsById(1L)).isTrue();
    }

    @Test
    void deleteById_delegatesToRepository() {
        store.deleteById(1L);
        verify(securityAlertRepository).deleteById(1L);
    }
}
