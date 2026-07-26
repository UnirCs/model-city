package com.modelcity.common.trails;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SystemTrailDtoTest {

    @Test
    void from_mapsAllFieldsFromView() {
        SystemTrailView view = mock(SystemTrailView.class);
        UUID eventId = UUID.randomUUID();
        OffsetDateTime occurredAt = OffsetDateTime.now();

        when(view.getEventId()).thenReturn(eventId);
        when(view.getEventType()).thenReturn("CITY_PLACE_CREATED");
        when(view.getOperationType()).thenReturn(OperationType.CREATE);
        when(view.getOccurredAt()).thenReturn(occurredAt);
        when(view.getCorrelationId()).thenReturn("corr-1");
        when(view.getResponsibleUserId()).thenReturn("agent-sub");
        when(view.getResponsibleUserRole()).thenReturn("MODEL-CITY-BACKOFFICE");
        when(view.getNeighbourhoodId()).thenReturn(5L);
        when(view.getZoneId()).thenReturn(1L);
        when(view.getResourceType()).thenReturn("CITY_PLACE");
        when(view.getResourceId()).thenReturn("10");
        when(view.getPayload()).thenReturn("{\"id\":10}");

        SystemTrailDto dto = SystemTrailDto.from(view);

        assertThat(dto.eventId()).isEqualTo(eventId);
        assertThat(dto.eventType()).isEqualTo("CITY_PLACE_CREATED");
        assertThat(dto.operationType()).isEqualTo(OperationType.CREATE);
        assertThat(dto.occurredAt()).isEqualTo(occurredAt);
        assertThat(dto.correlationId()).isEqualTo("corr-1");
        assertThat(dto.responsibleUserId()).isEqualTo("agent-sub");
        assertThat(dto.responsibleUserRole()).isEqualTo("MODEL-CITY-BACKOFFICE");
        assertThat(dto.neighbourhoodId()).isEqualTo(5L);
        assertThat(dto.zoneId()).isEqualTo(1L);
        assertThat(dto.resourceType()).isEqualTo("CITY_PLACE");
        assertThat(dto.resourceId()).isEqualTo("10");
        assertThat(dto.payload()).isEqualTo("{\"id\":10}");
    }
}
