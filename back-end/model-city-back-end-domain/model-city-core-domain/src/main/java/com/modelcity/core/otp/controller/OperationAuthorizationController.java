package com.modelcity.core.otp.controller;


import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.common.dto.OperationAuthorizationResponseDto;
import com.modelcity.core.otp.controller.model.CreateChallengeRequestDto;
import com.modelcity.core.otp.controller.model.ValidateChallengeRequestDto;
import com.modelcity.core.otp.usecase.BurnOperationAuthorizationUseCase;
import com.modelcity.core.otp.usecase.CreateChallengeUseCase;
import com.modelcity.core.otp.usecase.GetOperationAuthorizationUseCase;
import com.modelcity.core.otp.usecase.ValidateChallengeUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Operation authorization (OTP) endpoints.
 *
 * <p>Overridable base controller (abstract): the platform registers {@link DefaultOperationAuthorizationController}
 * as the default bean. A local deployment overrides by declaring its own {@code @RestController} that
 * {@code extends OperationAuthorizationController}; the default then backs off.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/operation-authorizations")
@ModelCityExtensionPoint
public abstract class OperationAuthorizationController<R extends CreateChallengeRequestDto, V extends ValidateChallengeRequestDto> {

    protected final CreateChallengeUseCase<R> createChallengeUseCase;
    protected final ValidateChallengeUseCase<V> validateChallengeUseCase;
    protected final GetOperationAuthorizationUseCase getOperationAuthorizationUseCase;
    protected final BurnOperationAuthorizationUseCase burnOperationAuthorizationUseCase;

    @PostMapping
    public ResponseEntity<OperationAuthorizationResponseDto> createChallenge(
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid R request) {
        OperationAuthorizationResponseDto response = createChallengeUseCase.execute(sub, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OperationAuthorizationResponseDto> getOperationAuthorization(@PathVariable UUID id) {
        return ResponseEntity.ok(getOperationAuthorizationUseCase.execute(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OperationAuthorizationResponseDto> validateChallenge(
            @PathVariable UUID id,
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid V request) {
        OperationAuthorizationResponseDto response = validateChallengeUseCase.execute(id, sub, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/burn")
    public ResponseEntity<OperationAuthorizationResponseDto> burnChallenge(@PathVariable UUID id) {
        return ResponseEntity.ok(burnOperationAuthorizationUseCase.execute(id));
    }
}

