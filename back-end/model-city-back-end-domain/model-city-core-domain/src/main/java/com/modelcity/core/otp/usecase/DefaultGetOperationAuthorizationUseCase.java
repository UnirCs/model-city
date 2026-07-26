package com.modelcity.core.otp.usecase;

import com.modelcity.common.dto.OperationAuthorizationResponseDto;
import com.modelcity.core.otp.repository.model.OperationAuthorization;
import com.modelcity.core.otp.repository.OperationAuthorizationRepository;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/** Default {@link GetOperationAuthorizationUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@Slf4j
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetOperationAuthorizationUseCase implements GetOperationAuthorizationUseCase {

    private final OperationAuthorizationRepository repository;

    @Override
    @Transactional(readOnly = true)
    public OperationAuthorizationResponseDto execute(UUID id) {
        log.debug("Fetching operation authorization id={}", id);
        OperationAuthorization auth = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authorization not found"));
        return toResponse(auth);
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
