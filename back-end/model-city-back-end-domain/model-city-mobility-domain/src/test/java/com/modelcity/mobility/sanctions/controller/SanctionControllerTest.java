package com.modelcity.mobility.sanctions.controller;

import com.modelcity.mobility.sanctions.controller.model.SanctionDto;
import com.modelcity.mobility.sanctions.controller.model.SanctionRequestDto;
import com.modelcity.mobility.sanctions.controller.model.SanctionSummaryDto;
import com.modelcity.mobility.sanctions.usecase.CreateSanctionUseCase;
import com.modelcity.mobility.sanctions.usecase.GetSanctionUseCase;
import com.modelcity.mobility.sanctions.usecase.GetSanctionsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SanctionControllerTest {

    @Mock CreateSanctionUseCase<SanctionDto, SanctionRequestDto> createSanctionUseCase;
    @Mock GetSanctionsUseCase<SanctionSummaryDto> getSanctionsUseCase;
    @Mock GetSanctionUseCase<SanctionDto> getSanctionUseCase;

    DefaultSanctionController controller;

    @BeforeEach
    void setUp() {
        controller = new DefaultSanctionController(createSanctionUseCase, getSanctionsUseCase, getSanctionUseCase);
    }

    @Test
    void createSanction_delegatesToUseCase() {
        SanctionRequestDto request = new SanctionRequestDto("1234ABC", 40.0, -3.0, "imgdata");
        controller.createSanction("agent-sub", request);
        verify(createSanctionUseCase).execute("agent-sub", request);
    }

    @Test
    void getSanctions_delegatesToUseCase() {
        OffsetDateTime from = OffsetDateTime.now().minusDays(1);
        OffsetDateTime to = OffsetDateTime.now();
        controller.getSanctions("agent-sub", "1234ABC", from, to, 1);
        verify(getSanctionsUseCase).execute("1234ABC", from, to, 1);
    }

    @Test
    void getSanction_delegatesToUseCase() {
        controller.getSanction(1L, "agent-sub");
        verify(getSanctionUseCase).execute(1L);
    }
}
