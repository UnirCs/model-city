package com.modelcity.core.otp.usecase;

import com.modelcity.common.dto.OperationAuthorizationResponseDto;
import com.modelcity.core.trails.SystemTrailGenerator;
import com.modelcity.core.otp.controller.model.CreateChallengeRequestDto;
import com.modelcity.core.users.repository.UserRepository;
import com.modelcity.core.otp.repository.model.OperationAuthorization;
import com.modelcity.core.otp.repository.model.OperationStatus;
import com.modelcity.core.otp.repository.OperationAuthorizationRepository;
import com.modelcity.core.otp.usecase.mail.OtpService;
import com.modelcity.core.security.CertificateVerificationTokenService;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** Default {@link CreateChallengeUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@Slf4j
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultCreateChallengeUseCase implements CreateChallengeUseCase<CreateChallengeRequestDto> {

    private static final int OTP_TTL_MINUTES = 2;
    private static final int DEFAULT_ATTEMPTS = 3;

    private final OperationAuthorizationRepository repository;
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final CertificateVerificationTokenService tokenService;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    @Transactional
    public OperationAuthorizationResponseDto execute(String sub, CreateChallengeRequestDto request) {
        String email = userRepository.findById(sub)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .getEmail();
        String otpHash = otpService.generateOtpHash(email);
        Instant now = Instant.now();

        // If a verification token is supplied, validate it (signature + expiry) and bind the verified
        // dni_hash to the challenge so the operation can later be deduplicated by DNI. The token is
        // infalsifiable because it is signed by core at the mTLS verification step.
        String dniHash = request.getVerificationToken() == null
                ? null
                : tokenService.verifyAndExtractDniHash(request.getVerificationToken());

        OperationAuthorization authorization = OperationAuthorization.builder()
                .operationAuthorizationId(UUID.randomUUID())
                .operationType(request.getOperationType())
                .resourceType(request.getResourceType())
                .resourceId(request.getResourceId())
                .userId(sub)
                .expiresAt(now.plus(OTP_TTL_MINUTES, ChronoUnit.MINUTES))
                .status(OperationStatus.PENDING)
                .otpHash(otpHash)
                .dniHash(dniHash)
                .attemptsRemaining(DEFAULT_ATTEMPTS)
                .createdAt(now)
                .build();

        repository.save(authorization);
        systemEventGenerator.operationAuthorizationCreated(authorization);
        log.info("Challenge created id={} for sub={}", authorization.getOperationAuthorizationId(), sub);
        return toResponse(authorization);
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
