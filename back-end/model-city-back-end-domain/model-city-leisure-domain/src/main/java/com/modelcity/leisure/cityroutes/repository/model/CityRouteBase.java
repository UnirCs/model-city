package com.modelcity.leisure.cityroutes.repository.model;

import com.modelcity.leisure.cityplaces.repository.model.CityPlace;
import com.modelcity.leisure.cityroutes.store.model.CityRouteView;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Invariant columns of a city route, shared by the platform default entity ({@link CityRoute}) and any
 * city-specific subclass. Kept as a {@code @MappedSuperclass} so a city that needs extra columns can declare
 * its own {@code @Entity} extending this base instead of forking it — see {@code CityRouteRepository}'s Javadoc.
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class CityRouteBase implements CityRouteView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Localizable fields below hold the default-locale (es) value; other locales live in {@link #translations}. */
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /** Target audience: e.g. FAMILY, KIDS, TOURIST, NIGHTLIFE, CULTURAL, ACCESSIBLE. */
    @Column(name = "target_audience", nullable = false, length = 64)
    private String targetAudience;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    /** Estimated total duration of the route in minutes (optional). */
    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<CityRoutePlace> routePlaces = new ArrayList<>();

    /** Non-default-locale translations of the localizable fields, keyed by language code. */
    @ElementCollection
    @CollectionTable(name = "city_route_translations", joinColumns = @JoinColumn(name = "route_id"))
    @MapKeyColumn(name = "locale")
    @BatchSize(size = 64)
    @Builder.Default
    private Map<String, CityRouteI18n> translations = new HashMap<>();

    /** Localizable fields of a city route for a single locale. */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CityRouteI18n implements CityRouteView.Translation {
        @Column(name = "name")
        private String name;

        @Column(name = "description", columnDefinition = "TEXT")
        private String description;
    }
}
