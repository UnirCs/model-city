package com.modelcity.core.otp.usecase;

import com.modelcity.common.dto.OperationAuthorizationResponseDto;
import com.modelcity.core.trails.SystemTrailGenerator;
import com.modelcity.core.otp.controller.model.ValidateChallengeRequestDto;
import com.modelcity.core.otp.repository.model.OperationAuthorization;
import com.modelcity.core.otp.repository.model.OperationStatus;
import com.modelcity.core.otp.repository.OperationAuthorizationRepository;
import com.modelcity.core.otp.usecase.mail.OtpService;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

/** Default {@link ValidateChallengeUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@Slf4j
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultValidateChallengeUseCase implements ValidateChallengeUseCase<ValidateChallengeRequestDto> {

    private final OperationAuthorizationRepository repository;
    private final OtpService otpService;
    private final RegenerateOtpUseCase regenerateOtpUseCase;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public OperationAuthorizationResponseDto execute(UUID id, String sub, ValidateChallengeRequestDto request) {

        OperationAuthorization auth = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Challenge not found"));

        if (!auth.getUserId().equals(sub)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Challenge does not belong to the requesting user");
        }

        if (auth.getStatus() == OperationStatus.VERIFIED || auth.getStatus() == OperationStatus.BURNT) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(409), "Challenge has already been verified or consumed");
        }

        if (auth.getStatus() == OperationStatus.EXPIRED || Instant.now().isAfter(auth.getExpiresAt())) {
            auth.setStatus(OperationStatus.EXPIRED);
            repository.save(auth);
            throw new ResponseStatusException(HttpStatusCode.valueOf(410), "Challenge has expired");
        }

        if (!auth.getOperationType().equals(request.getOperationType())
                || !auth.getResourceType().equals(request.getResourceType())
                || !auth.getResourceId().equals(request.getResourceId())) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422), "Operation or resource mismatch");
        }

        if (!otpService.validateOtpHash(request.getOtp(), auth.getOtpHash())) {
            return handleWrongOtp(auth);
        }

        auth.setStatus(OperationStatus.VERIFIED);
        repository.save(auth);
        systemEventGenerator.operationAuthorizationVerified(auth);
        log.info("Challenge id={} validated successfully for sub={}", id, sub);
        return toResponse(auth);
    }

    private OperationAuthorizationResponseDto handleWrongOtp(OperationAuthorization auth) {
        int remaining = auth.getAttemptsRemaining() - 1;
        auth.setAttemptsRemaining(remaining);

        if (remaining <= 0) {
            auth.setStatus(OperationStatus.EXPIRED);
            repository.save(auth);
            log.warn("Challenge id={} exhausted all attempts — marked as EXPIRED", auth.getOperationAuthorizationId());
            throw new ResponseStatusException(HttpStatusCode.valueOf(422), "Invalid OTP. No attempts remaining. Challenge expired.");
        }

        regenerateOtpUseCase.execute(auth);
        log.warn("Wrong OTP for challenge id={}, {} attempt(s) remaining. New OTP sent.", auth.getOperationAuthorizationId(), remaining);
        throw new ResponseStatusException(HttpStatusCode.valueOf(422),
                "Invalid OTP. " + remaining + " attempt(s) remaining. A new code has been sent to your email.");
    }

    private OperationAuthorizationResponseDto toResponse(OperationAuthorization auth) {
        return new OperationAuthorizationResponseDto(
                auth.getOperationAuthorizationId(),
                auth.getOperationType(),
                auth.getResourceType(),
                auth.getResourceId(),
                auth.getUserId(),
                auth.getExpiresAt(),
                auth.getStatus().name(),
                auth.getAttemptsRemaining(),
                auth.getCreatedAt(),
                auth.getDniHash()
        );
    }
}
