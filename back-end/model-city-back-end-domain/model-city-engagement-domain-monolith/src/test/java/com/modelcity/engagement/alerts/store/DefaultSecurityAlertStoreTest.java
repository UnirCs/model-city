package com.modelcity.engagement.alerts.store;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.core.users.repository.NeighbourhoodRepository;
import com.modelcity.core.users.repository.ZoneRepository;
import com.modelcity.core.users.repository.model.Neighbourhood;
import com.modelcity.core.users.repository.model.Zone;
import com.modelcity.engagement.alerts.controller.model.SecurityAlertRequestDto;
import com.modelcity.engagement.alerts.repository.SecurityAlertRepository;
import com.modelcity.engagement.alerts.repository.model.AlertSeverity;
import com.modelcity.engagement.alerts.repository.model.SecurityAlert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DefaultSecurityAlertStoreTest {

    @Mock
    SecurityAlertRepository<SecurityAlert> securityAlertRepository;

    @Mock
    ZoneRepository zoneRepository;

    @Mock
    NeighbourhoodRepository neighbourhoodRepository;

    DefaultSecurityAlertStore store;

    @BeforeEach
    void setUp() {
        store = new DefaultSecurityAlertStore(securityAlertRepository, zoneRepository, neighbourhoodRepository);
    }

    private SecurityAlertRequestDto buildRequest(Long neighbourhoodId) {
        SecurityAlertRequestDto request = new SecurityAlertRequestDto();
        request.setTitle(Map.of("es", "Alerta", "en", "Alert"));
        request.setSeverity(AlertSeverity.MEDIUM);
        request.setDescription(Map.of("es", "Descripción"));
        request.setLatitude(40.4);
        request.setLongitude(-3.7);
        request.setZoneId(1L);
        request.setNeighbourhoodId(neighbourhoodId);
        request.setExpiresAt(OffsetDateTime.now().plusHours(24));
        return request;
    }

    @Test
    void create_resolvesZoneAndNeighbourhood() {
        Zone zone = new Zone();
        zone.setId(1L);
        Neighbourhood neighbourhood = new Neighbourhood();
        neighbourhood.setId(5L);

        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(neighbourhoodRepository.findById(5L)).thenReturn(Optional.of(neighbourhood));
        when(securityAlertRepository.save(any(SecurityAlert.class))).thenAnswer(inv -> inv.getArgument(0));

        SecurityAlert result = store.create(buildRequest(5L));

        assertThat(result.getZoneId()).isEqualTo(1L);
        assertThat(result.getNeighbourhoodId()).isEqualTo(5L);
        assertThat(result.getTitle()).isEqualTo("Alerta");
        assertThat(result.getTranslations()).containsKey("en");
    }

    @Test
    void create_withoutNeighbourhood_leavesNeighbourhoodNull() {
        Zone zone = new Zone();
        zone.setId(1L);
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(securityAlertRepository.save(any(SecurityAlert.class))).thenAnswer(inv -> inv.getArgument(0));

        SecurityAlert result = store.create(buildRequest(null));

        assertThat(result.getNeighbourhoodId()).isNull();
        verify(neighbourhoodRepository, never()).findById(any());
    }

    @Test
    void create_zoneNotFound_throwsResourceNotFound() {
        when(zoneRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> store.create(buildRequest(null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_neighbourhoodNotFound_throwsResourceNotFound() {
        Zone zone = new Zone();
        zone.setId(1L);
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(neighbourhoodRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> store.create(buildRequest(5L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findActive_delegatesToRepository() {
        OffsetDateTime now = OffsetDateTime.now();
        store.findActive(now, null);
        verify(securityAlertRepository).findByExpiresAtAfterOrderBySeverityAscCreatedAtDesc(now, null);
    }

    @Test
    void deleteById_delegatesToRepository() {
        store.deleteById(1L);
        verify(securityAlertRepository).deleteById(1L);
    }
}
