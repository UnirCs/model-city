package com.modelcity.core.trails.controller;

import com.modelcity.common.trails.SystemTrailQuery;
import com.modelcity.core.trails.usecase.GetSystemTrailsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
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

        controller.list("admin-sub", "USER_REGISTERED", "user-sub", from, to, 2);

        ArgumentCaptor<SystemTrailQuery> captor = ArgumentCaptor.forClass(SystemTrailQuery.class);
        verify(getSystemTrailsUseCase).execute(captor.capture(), eq(2));

        SystemTrailQuery query = captor.getValue();
        assertThat(query.eventType()).isEqualTo("USER_REGISTERED");
        assertThat(query.responsibleUserId()).isEqualTo("user-sub");
    }
}
