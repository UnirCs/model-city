package com.modelcity.engagement.alerts.store;

import com.modelcity.engagement.alerts.controller.model.SecurityAlertRequestDto;
import com.modelcity.engagement.alerts.repository.SecurityAlertRepository;
import com.modelcity.engagement.alerts.repository.model.SecurityAlert;
import com.modelcity.engagement.alerts.store.model.SecurityAlertView;
import com.modelcity.common.i18n.LocalizedText;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Microservice JPA adapter for the security-alert persistence port: stores zone/neighbourhood as soft
 * references (no FK, the zones live in another service); the default {@link SecurityAlertStore} bean.
 */
@Component
@ModelCityDisabledIfInherited
@RequiredArgsConstructor
public class DefaultSecurityAlertStore implements SecurityAlertStore<SecurityAlert, SecurityAlertRequestDto> {

    private final SecurityAlertRepository<SecurityAlert> securityAlertRepository;

    @Override
    public Page<SecurityAlert> findActive(OffsetDateTime now, Pageable pageable) {
        return securityAlertRepository.findByExpiresAtAfterOrderBySeverityAscCreatedAtDesc(now, pageable);
    }

    @Override
    public SecurityAlert create(SecurityAlertRequestDto request) {
        SecurityAlert alert = SecurityAlert.builder()
                .title(LocalizedText.requireDefault("title", request.getTitle()))
                .severity(request.getSeverity())
                .description(LocalizedText.requireDefault("description", request.getDescription()))
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .zoneId(request.getZoneId())
                .neighbourhoodId(request.getNeighbourhoodId())
                .createdAt(OffsetDateTime.now())
                .expiresAt(request.getExpiresAt())
                .build();
        applyTranslations(alert, request);
        return securityAlertRepository.save(alert);
    }

    private void applyTranslations(SecurityAlert alert, SecurityAlertRequestDto request) {
        Map<String, String> title = LocalizedText.nonDefault(request.getTitle());
        Map<String, String> description = LocalizedText.nonDefault(request.getDescription());
        Set<String> locales = new HashSet<>();
        locales.addAll(title.keySet());
        locales.addAll(description.keySet());
        Map<String, SecurityAlert.SecurityAlertI18n> translations = new HashMap<>();
        for (String locale : locales) {
            translations.put(locale, new SecurityAlert.SecurityAlertI18n(title.get(locale), description.get(locale)));
        }
        alert.getTranslations().clear();
        alert.getTranslations().putAll(translations);
    }

    @Override
    public Optional<SecurityAlert> findById(Long id) {
        return securityAlertRepository.findById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return securityAlertRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        securityAlertRepository.deleteById(id);
    }
}
