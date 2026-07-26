package com.modelcity.core.otp.controller;

import com.modelcity.common.dto.OperationAuthorizationResponseDto;
import com.modelcity.core.otp.controller.model.CreateChallengeRequestDto;
import com.modelcity.core.otp.controller.model.ValidateChallengeRequestDto;
import com.modelcity.core.otp.usecase.BurnOperationAuthorizationUseCase;
import com.modelcity.core.otp.usecase.CreateChallengeUseCase;
import com.modelcity.core.otp.usecase.GetOperationAuthorizationUseCase;
import com.modelcity.core.otp.usecase.ValidateChallengeUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationAuthorizationControllerTest {

    @Mock CreateChallengeUseCase<CreateChallengeRequestDto> createChallengeUseCase;
    @Mock ValidateChallengeUseCase<ValidateChallengeRequestDto> validateChallengeUseCase;
    @Mock GetOperationAuthorizationUseCase getOperationAuthorizationUseCase;
    @Mock BurnOperationAuthorizationUseCase burnOperationAuthorizationUseCase;

    DefaultOperationAuthorizationController controller;

    @BeforeEach
    void setUp() {
        controller = new DefaultOperationAuthorizationController(createChallengeUseCase, validateChallengeUseCase,
                getOperationAuthorizationUseCase, burnOperationAuthorizationUseCase);
    }

    private OperationAuthorizationResponseDto buildResponse(UUID id) {
        return new OperationAuthorizationResponseDto(id, "CONFIRM_ANSWER", "public-question", "1",
                "user-sub", Instant.now().plusSeconds(120), "PENDING", 3, Instant.now(), null);
    }

    @Test
    void createChallenge_returnsCreatedWithResponse() {
        CreateChallengeRequestDto request = new CreateChallengeRequestDto("CONFIRM_ANSWER", "public-question", "1", null);
        UUID id = UUID.randomUUID();
        OperationAuthorizationResponseDto response = buildResponse(id);
        when(createChallengeUseCase.execute("user-sub", request)).thenReturn(response);

        ResponseEntity<OperationAuthorizationResponseDto> result = controller.createChallenge("user-sub", request);

        assertThat(result.getStatusCode().value()).isEqualTo(201);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void getOperationAuthorization_returnsOkWithResponse() {
        UUID id = UUID.randomUUID();
        OperationAuthorizationResponseDto response = buildResponse(id);
        when(getOperationAuthorizationUseCase.execute(id)).thenReturn(response);

        ResponseEntity<OperationAuthorizationResponseDto> result = controller.getOperationAuthorization(id);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void validateChallenge_delegatesToUseCase() {
        UUID id = UUID.randomUUID();
        ValidateChallengeRequestDto request = new ValidateChallengeRequestDto("123456", "CONFIRM_ANSWER", "public-question", "1");
        OperationAuthorizationResponseDto response = buildResponse(id);
        when(validateChallengeUseCase.execute(id, "user-sub", request)).thenReturn(response);

        ResponseEntity<OperationAuthorizationResponseDto> result = controller.validateChallenge(id, "user-sub", request);

        assertThat(result.getBody()).isEqualTo(response);
        verify(validateChallengeUseCase).execute(id, "user-sub", request);
    }

    @Test
    void burnChallenge_delegatesToUseCase() {
        UUID id = UUID.randomUUID();
        OperationAuthorizationResponseDto response = buildResponse(id);
        when(burnOperationAuthorizationUseCase.execute(id)).thenReturn(response);

        ResponseEntity<OperationAuthorizationResponseDto> result = controller.burnChallenge(id);

        assertThat(result.getBody()).isEqualTo(response);
    }
}
