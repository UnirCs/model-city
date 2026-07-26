package com.modelcity.engagement.alerts.usecase;

import com.modelcity.engagement.alerts.controller.model.SecurityAlertDto;
import com.modelcity.engagement.alerts.controller.model.SecurityAlertRequestDto;
import com.modelcity.engagement.alerts.store.SecurityAlertStore;
import com.modelcity.engagement.alerts.store.model.SecurityAlertView;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/** Default {@link GetSecurityAlertsUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetSecurityAlertsUseCase implements GetSecurityAlertsUseCase<SecurityAlertDto> {

    private static final int PAGE_SIZE = 4;

    private final SecurityAlertStore<? extends SecurityAlertView, SecurityAlertRequestDto> securityAlertStore;

    @Override
    @Cacheable(cacheNames = CacheNames.SECURITY_ALERTS, key = "#locale + '-' + #page")
    @Transactional(readOnly = true)
    public Page<SecurityAlertDto> execute(int page, String locale) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("severity").ascending().and(Sort.by("createdAt").descending()));
        return securityAlertStore
                .findActive(OffsetDateTime.now(), pageable)
                .map(a -> SecurityAlertDto.from(a, locale));
    }
}
