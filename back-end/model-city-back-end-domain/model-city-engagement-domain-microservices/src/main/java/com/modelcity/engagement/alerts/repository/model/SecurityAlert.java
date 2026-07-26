package com.modelcity.engagement.alerts.repository.model;

import com.modelcity.engagement.alerts.store.model.SecurityAlertView;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Microservices flavour of the security alert: zone/neighbourhood are plain writable {@code Long} columns
 * (soft references — the zones live in another service), on top of the invariant mapping in
 * {@link SecurityAlertBase}.
 */
@Entity
@Table(name = "security_alerts")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class SecurityAlert extends SecurityAlertBase implements SecurityAlertView {

    /** Soft ref to the zone this alert applies to. Always required. */
    @Column(name = "zone_id", nullable = false)
    private Long zoneId;

    /** Soft ref to a specific neighbourhood. Null means the whole zone. */
    @Column(name = "neighbourhood_id")
    private Long neighbourhoodId;
}
