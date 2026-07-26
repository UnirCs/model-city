package com.modelcity.engagement.alerts.repository.model;

import com.modelcity.engagement.alerts.store.model.SecurityAlertView;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.MapKeyColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Topology-invariant mapping for a citizen security alert. Every scalar column is identical in the monolith
 * and the microservices, except the zone/neighbourhood reference: in the microservices flavour it is a plain
 * writable {@code Long} column, while in the monolith it is a real mandatory {@code @ManyToOne Zone} (plus an
 * optional {@code @ManyToOne Neighbourhood}) with no underlying {@code Long} column at all — so unlike the
 * usual soft-FK convention, there is no shared writable column to keep here. The concrete {@code @Entity
 * SecurityAlert} in each per-topology domain library declares {@code zoneId}/{@code neighbourhoodId} itself
 * (as a plain column or as a {@code @Transient} getter deriving from the association).
 *
 * <p>This class intentionally does <em>not</em> implement {@code SecurityAlertView} — it cannot provide
 * {@code getZoneId()}/{@code getNeighbourhoodId()} without committing to one of the two topology-specific
 * shapes. The concrete subclass in each topology library implements the view.
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class SecurityAlertBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Localizable fields below hold the default-locale (es) value; other locales live in {@link #translations}. */
    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AlertSeverity severity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    /** Alert stops being visible after this timestamp. */
    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    /** Non-default-locale translations of the localizable fields, keyed by language code. */
    @ElementCollection
    @CollectionTable(name = "security_alert_translations", joinColumns = @JoinColumn(name = "alert_id"))
    @MapKeyColumn(name = "locale")
    @BatchSize(size = 64)
    @Builder.Default
    private Map<String, SecurityAlertI18n> translations = new HashMap<>();

    /** Localizable fields of a security alert for a single locale. */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityAlertI18n implements SecurityAlertView.Translation {
        @Column(name = "title")
        private String title;

        @Column(name = "description", columnDefinition = "TEXT")
        private String description;
    }
}
