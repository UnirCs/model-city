package com.modelcity.engagement.trails.usecase;

import com.modelcity.common.trails.SystemTrailDto;
import com.modelcity.common.trails.SystemTrailQuery;
import com.modelcity.common.trails.SystemTrailView;
import com.modelcity.engagement.trails.store.EngagementSystemTrailStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EngagementTrailsUseCasesTest {

    @Mock
    EngagementSystemTrailStore store;

    DefaultGetSystemTrailsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DefaultGetSystemTrailsUseCase(store);
    }

    private SystemTrailQuery emptyQuery() {
        return new SystemTrailQuery(null, null, null, null);
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_returnsPageOfTrails() {
        SystemTrailView trail = mock(SystemTrailView.class);
        when(trail.getEventId()).thenReturn(UUID.randomUUID());
        when(trail.getEventType()).thenReturn("CIVIC_QUESTION_CREATED");
        when(trail.getResourceType()).thenReturn("CIVIC_QUESTION");
        when(trail.getResourceId()).thenReturn("1");
        when(trail.getResponsibleUserId()).thenReturn("agent-sub");
        when(trail.getOccurredAt()).thenReturn(OffsetDateTime.now());

        Page<SystemTrailView> page = new PageImpl<>(List.of(trail));
        doReturn(page).when(store).search(any(SystemTrailQuery.class), any(Pageable.class));

        Page<SystemTrailDto> result = useCase.execute(emptyQuery(), 0);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).eventType()).isEqualTo("CIVIC_QUESTION_CREATED");
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_emptyResult_returnsEmptyPage() {
        Page<SystemTrailView> emptyPage = new PageImpl<>(List.of());
        doReturn(emptyPage).when(store).search(any(SystemTrailQuery.class), any(Pageable.class));

        Page<SystemTrailDto> result = useCase.execute(emptyQuery(), 0);

        assertThat(result.getTotalElements()).isZero();
    }
}
