package com.modelcity.leisure.publicspaces.repository.model;

import com.modelcity.leisure.publicspaces.model.ReservableResourceView;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;

import java.util.HashMap;
import java.util.Map;

/**
 * Invariant columns of a reservable resource, shared by the platform default entity
 * ({@link ReservableResource}) and any city-specific subclass. Kept as a {@code @MappedSuperclass} so a city
 * that needs extra columns can declare its own {@code @Entity} extending this base instead of forking it —
 * see {@code ReservableResourceRepository}'s Javadoc.
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class ReservableResourceBase implements ReservableResourceView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_space_id", nullable = false)
    private Long publicSpaceId;

    /** Localizable fields below hold the default-locale (es) value; other locales live in {@link #translations}. */
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Free-form category (FOOTBALL_FIELD, BASKETBALL_COURT, TENNIS_COURT, PADEL_COURT...). */
    @Column(name = "resource_type", nullable = false, length = 64)
    private String resourceType;

    /** Soft-delete flag. */
    @Column(nullable = false)
    private boolean active;

    /** Non-default-locale translations of the localizable fields, keyed by language code. */
    @ElementCollection
    @CollectionTable(name = "reservable_resource_translations", joinColumns = @JoinColumn(name = "resource_id"))
    @MapKeyColumn(name = "locale")
    @BatchSize(size = 64)
    @Builder.Default
    private Map<String, ReservableResourceI18n> translations = new HashMap<>();

    /** Localizable fields of a reservable resource for a single locale. */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservableResourceI18n implements ReservableResourceView.Translation {
        @Column(name = "name")
        private String name;

        @Column(name = "description", columnDefinition = "TEXT")
        private String description;
    }
}
