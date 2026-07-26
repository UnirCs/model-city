package com.modelcity.engagement.alerts.usecase;

import com.modelcity.engagement.alerts.controller.model.SecurityAlertDto;
import com.modelcity.engagement.alerts.controller.model.SecurityAlertRequestDto;
import com.modelcity.engagement.alerts.store.model.SecurityAlertView;
import com.modelcity.engagement.alerts.store.SecurityAlertStore;
import com.modelcity.engagement.trails.SystemTrailGenerator;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

/** Default {@link CreateSecurityAlertUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultCreateSecurityAlertUseCase implements CreateSecurityAlertUseCase<SecurityAlertDto, SecurityAlertRequestDto> {

    private final SecurityAlertStore<? extends SecurityAlertView, SecurityAlertRequestDto> securityAlertStore;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    @CacheEvict(cacheNames = CacheNames.SECURITY_ALERTS, allEntries = true)
    public SecurityAlertDto execute(String sub, SecurityAlertRequestDto request, String locale) {
        if (!request.getExpiresAt().isAfter(OffsetDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "expiresAt must be in the future");
        }

        SecurityAlertView saved = securityAlertStore.create(request);
        systemEventGenerator.securityAlertCreated(sub, saved);
        log.info("SecurityAlert created id={} severity={} by sub={}", saved.getId(), saved.getSeverity(), sub);
        return SecurityAlertDto.from(saved, locale);
    }
}
