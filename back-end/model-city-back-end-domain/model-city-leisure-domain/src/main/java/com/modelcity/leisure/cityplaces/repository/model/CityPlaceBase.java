package com.modelcity.leisure.cityplaces.repository.model;

import com.modelcity.leisure.cityplaces.store.model.CityPlaceView;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;

import java.util.HashMap;
import java.util.Map;

/**
 * Invariant columns of a city place, shared by the platform default entity ({@link CityPlace}) and any
 * city-specific subclass. Kept as a {@code @MappedSuperclass} (rather than folding everything into
 * {@link CityPlace}) so a city that needs extra columns can declare its own {@code @Entity} extending this
 * base — the same technique used for the topology-divergent entities (e.g. {@code EventTicketBase}) — without
 * forking the platform-owned columns. See the "Conventions" section of
 * {@code docs/PERSISTENCE-UPSTREAM-PLAYBOOK.md}.
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class CityPlaceBase implements CityPlaceView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Localizable fields below hold the default-locale (es) value; other locales live in {@link #translations}. */
    @Column(nullable = false)
    private String name;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 512)
    private String address;

    /** Up to three photo URLs are stored as dedicated columns to keep the schema simple. */
    @Column(name = "photo_url_1", length = 2048)
    private String photoUrl1;

    @Column(name = "photo_url_2", length = 2048)
    private String photoUrl2;

    @Column(name = "photo_url_3", length = 2048)
    private String photoUrl3;

    @Column(name = "access_info", columnDefinition = "TEXT")
    private String accessInfo;

    @Column(name = "accessibility_info", columnDefinition = "TEXT")
    private String accessibilityInfo;

    /** Free-form category (e.g. MONUMENT, MUSEUM, PARK, SQUARE, VIEWPOINT). */
    @Column(length = 64)
    private String category;

    /** Estimated visit duration in minutes (optional). */
    @Column(name = "visit_duration_minutes")
    private Integer visitDurationMinutes;

    /** Non-default-locale translations of the localizable fields, keyed by language code. */
    @ElementCollection
    @CollectionTable(name = "city_place_translations", joinColumns = @JoinColumn(name = "place_id"))
    @MapKeyColumn(name = "locale")
    @BatchSize(size = 64)
    @Builder.Default
    private Map<String, CityPlaceI18n> translations = new HashMap<>();

    /** Localizable fields of a city place for a single locale. */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CityPlaceI18n implements CityPlaceView.Translation {
        @Column(name = "name")
        private String name;

        @Column(name = "description", columnDefinition = "TEXT")
        private String description;

        @Column(name = "address", length = 512)
        private String address;

        @Column(name = "access_info", columnDefinition = "TEXT")
        private String accessInfo;

        @Column(name = "accessibility_info", columnDefinition = "TEXT")
        private String accessibilityInfo;
    }
}

