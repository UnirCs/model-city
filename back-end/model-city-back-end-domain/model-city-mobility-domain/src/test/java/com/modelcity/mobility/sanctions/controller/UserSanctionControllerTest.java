package com.modelcity.mobility.sanctions.controller;

import com.modelcity.mobility.sanctions.controller.model.SanctionDto;
import com.modelcity.mobility.sanctions.controller.model.SanctionSummaryDto;
import com.modelcity.mobility.sanctions.usecase.GetUserSanctionUseCase;
import com.modelcity.mobility.sanctions.usecase.GetUserSanctionsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserSanctionControllerTest {

    @Mock GetUserSanctionsUseCase<SanctionSummaryDto> getUserSanctionsUseCase;
    @Mock GetUserSanctionUseCase<SanctionDto> getUserSanctionUseCase;

    DefaultUserSanctionController controller;

    @BeforeEach
    void setUp() {
        controller = new DefaultUserSanctionController(getUserSanctionsUseCase, getUserSanctionUseCase);
    }

    @Test
    void getUserSanctions_delegatesToUseCase() {
        PageRequest pageable = PageRequest.of(0, 10);
        controller.getUserSanctions("user-sub", "user-sub", pageable);
        verify(getUserSanctionsUseCase).execute("user-sub", "user-sub", pageable);
    }

    @Test
    void getUserSanction_delegatesToUseCase() {
        controller.getUserSanction("user-sub", 1L, "user-sub");
        verify(getUserSanctionUseCase).execute("user-sub", "user-sub", 1L);
    }
}
