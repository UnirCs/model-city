package com.modelcity.engagement.alerts.repository.model;

import com.modelcity.engagement.alerts.store.model.SecurityAlertView;
import com.modelcity.core.users.repository.model.Neighbourhood;
import com.modelcity.core.users.repository.model.Zone;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Monolith flavour of the security alert: zone/neighbourhood are real, mandatory/optional
 * {@code @ManyToOne} associations (no underlying {@code Long} column), on top of the invariant mapping in
 * {@link SecurityAlertBase}. {@code getZoneId()}/{@code getNeighbourhoodId()} are derived, read-only
 * conveniences exposed for DTO mapping.
 */
@Entity
@Table(name = "security_alerts")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class SecurityAlert extends SecurityAlertBase implements SecurityAlertView {

    /** Zone the alert applies to. Mandatory. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    /** Specific neighbourhood. Null means the whole zone. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "neighbourhood_id")
    private Neighbourhood neighbourhood;

    /** Convenience getter exposed for DTO mapping. */
    @Transient
    public Long getZoneId() {
        return zone == null ? null : zone.getId();
    }

    /** Convenience getter exposed for DTO mapping. */
    @Transient
    public Long getNeighbourhoodId() {
        return neighbourhood == null ? null : neighbourhood.getId();
    }
}
