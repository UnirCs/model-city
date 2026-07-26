package com.modelcity.engagement.alerts.controller.model;

import com.modelcity.engagement.alerts.store.model.SecurityAlertView;
import com.modelcity.engagement.alerts.repository.model.AlertSeverity;
import com.modelcity.common.i18n.LocalizedText;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/** Full representation of a security alert. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SecurityAlertDto {

    private Long id;
    private String title;
    private AlertSeverity severity;
    private String description;
    private Double latitude;
    private Double longitude;
    private Long zoneId;
    private Long neighbourhoodId;
    private OffsetDateTime createdAt;
    private OffsetDateTime expiresAt;

    public static SecurityAlertDto from(SecurityAlertView a, String locale) {
        SecurityAlertView.Translation t = a.getTranslations().get(locale);
        return new SecurityAlertDto(
                a.getId(),
                LocalizedText.resolve(a.getTitle(), t == null ? null : t.getTitle()),
                a.getSeverity(),
                LocalizedText.resolve(a.getDescription(), t == null ? null : t.getDescription()),
                a.getLatitude(), a.getLongitude(),
                a.getZoneId(), a.getNeighbourhoodId(),
                a.getCreatedAt(), a.getExpiresAt()
        );
    }
}
