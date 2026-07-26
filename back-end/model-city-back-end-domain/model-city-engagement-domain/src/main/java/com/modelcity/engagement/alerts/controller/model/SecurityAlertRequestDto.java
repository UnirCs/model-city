package com.modelcity.engagement.alerts.controller.model;

import com.modelcity.engagement.alerts.repository.model.AlertSeverity;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Request body for POST /security-alerts. The localizable {@code title} and {@code description} are
 * multi-locale maps ({@code locale -> text}) with a mandatory {@code es} entry.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SecurityAlertRequestDto {

    @NotEmpty private Map<String, String> title;
    @NotNull private AlertSeverity severity;
    @NotEmpty private Map<String, String> description;
    @NotNull private Double latitude;
    @NotNull private Double longitude;
    @NotNull private Long zoneId;
    private Long neighbourhoodId;
    @NotNull private OffsetDateTime expiresAt;
}
