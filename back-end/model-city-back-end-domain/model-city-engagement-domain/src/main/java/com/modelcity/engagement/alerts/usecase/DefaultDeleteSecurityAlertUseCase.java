package com.modelcity.engagement.alerts.usecase;

import com.modelcity.engagement.alerts.controller.model.SecurityAlertRequestDto;
import com.modelcity.engagement.alerts.store.SecurityAlertStore;
import com.modelcity.engagement.alerts.store.model.SecurityAlertView;
import com.modelcity.engagement.trails.SystemTrailGenerator;
import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link DeleteSecurityAlertUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultDeleteSecurityAlertUseCase implements DeleteSecurityAlertUseCase {

    private final SecurityAlertStore<? extends SecurityAlertView, SecurityAlertRequestDto> securityAlertStore;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    @CacheEvict(cacheNames = CacheNames.SECURITY_ALERTS, allEntries = true)
    public void execute(Long id, String sub) {
        SecurityAlertView alert = securityAlertStore.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SecurityAlert", id));
        securityAlertStore.deleteById(id);
        systemEventGenerator.securityAlertDeleted(sub, alert);
        log.info("SecurityAlert id={} deleted by sub={}", id, sub);
    }
}
