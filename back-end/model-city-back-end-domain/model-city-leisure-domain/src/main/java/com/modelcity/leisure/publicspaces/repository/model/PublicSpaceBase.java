package com.modelcity.leisure.publicspaces.repository.model;

import com.modelcity.leisure.publicspaces.model.PublicSpaceView;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;

import java.util.HashMap;
import java.util.Map;

/**
 * Invariant columns of a public space, shared by the platform default entity ({@link PublicSpace}) and any
 * city-specific subclass. Kept as a {@code @MappedSuperclass} so a city that needs extra columns can declare
 * its own {@code @Entity} extending this base instead of forking it — see {@code PublicSpaceRepository}'s
 * Javadoc.
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class PublicSpaceBase implements PublicSpaceView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Localizable fields below hold the default-locale (es) value; other locales live in {@link #translations}. */
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 512)
    private String address;

    private Double latitude;

    private Double longitude;

    @Column(name = "photo_url_1", length = 2048)
    private String photoUrl1;

    @Column(name = "photo_url_2", length = 2048)
    private String photoUrl2;

    @Column(name = "photo_url_3", length = 2048)
    private String photoUrl3;

    /** Soft-delete flag. */
    @Column(nullable = false)
    private boolean active;

    /** Non-default-locale translations of the localizable fields, keyed by language code. */
    @ElementCollection
    @CollectionTable(name = "public_space_translations", joinColumns = @JoinColumn(name = "space_id"))
    @MapKeyColumn(name = "locale")
    @BatchSize(size = 64)
    @Builder.Default
    private Map<String, PublicSpaceI18n> translations = new HashMap<>();

    /** Localizable fields of a public space for a single locale. */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PublicSpaceI18n implements PublicSpaceView.Translation {
        @Column(name = "name")
        private String name;

        @Column(name = "description", columnDefinition = "TEXT")
        private String description;

        @Column(name = "address", length = 512)
        private String address;
    }
}
