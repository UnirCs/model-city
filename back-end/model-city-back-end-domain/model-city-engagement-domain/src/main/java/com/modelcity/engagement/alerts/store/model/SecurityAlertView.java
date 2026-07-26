package com.modelcity.engagement.alerts.store.model;

import com.modelcity.engagement.alerts.repository.model.AlertSeverity;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Read-only view of a security alert, exposed by the persistence adapter so the domain
 * (controllers, use cases, DTOs) stays decoupled from the deployment-specific JPA entity.
 * Each deployment maps zone/neighbourhood differently (soft id vs FK) but exposes the same surface.
 */
public interface SecurityAlertView {
    Long getId();
    String getTitle();
    AlertSeverity getSeverity();
    String getDescription();
    Double getLatitude();
    Double getLongitude();
    Long getZoneId();
    Long getNeighbourhoodId();
    OffsetDateTime getCreatedAt();
    OffsetDateTime getExpiresAt();

    /** Per-locale translations of the localizable fields, keyed by language code. */
    Map<String, ? extends Translation> getTranslations();

    /** Localizable fields of a security alert for a single non-default locale. */
    interface Translation {
        String getTitle();
        String getDescription();
    }
}
