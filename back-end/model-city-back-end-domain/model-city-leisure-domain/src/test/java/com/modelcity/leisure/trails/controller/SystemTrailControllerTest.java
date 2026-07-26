package com.modelcity.leisure.trails.controller;

import com.modelcity.common.trails.SystemTrailQuery;
import com.modelcity.leisure.trails.usecase.GetSystemTrailsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SystemTrailControllerTest {

    @Mock GetSystemTrailsUseCase getSystemTrailsUseCase;

    DefaultSystemTrailController controller;

    @BeforeEach
    void setUp() {
        controller = new DefaultSystemTrailController(getSystemTrailsUseCase);
    }

    @Test
    void list_buildsQueryFromParamsAndDelegates() {
        OffsetDateTime from = OffsetDateTime.now().minusDays(1);
        OffsetDateTime to = OffsetDateTime.now();

        controller.list("admin-sub", "CITY_PLACE_CREATED", "agent-sub", from, to, 2);

        ArgumentCaptor<SystemTrailQuery> captor = ArgumentCaptor.forClass(SystemTrailQuery.class);
        verify(getSystemTrailsUseCase).execute(captor.capture(), org.mockito.ArgumentMatchers.eq(2));

        SystemTrailQuery query = captor.getValue();
        assertThat(query.eventType()).isEqualTo("CITY_PLACE_CREATED");
        assertThat(query.responsibleUserId()).isEqualTo("agent-sub");
        assertThat(query.from()).isEqualTo(from);
        assertThat(query.to()).isEqualTo(to);
    }

    @Test
    void list_withoutFilters_buildsEmptyQuery() {
        controller.list("admin-sub", null, null, null, null, 0);

        ArgumentCaptor<SystemTrailQuery> captor = ArgumentCaptor.forClass(SystemTrailQuery.class);
        verify(getSystemTrailsUseCase).execute(captor.capture(), org.mockito.ArgumentMatchers.eq(0));

        SystemTrailQuery query = captor.getValue();
        assertThat(query.eventType()).isNull();
        assertThat(query.responsibleUserId()).isNull();
    }
}
