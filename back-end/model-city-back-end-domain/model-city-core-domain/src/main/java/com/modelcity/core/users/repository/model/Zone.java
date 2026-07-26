package com.modelcity.core.users.repository.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A zone is a named grouping of {@link Neighbourhood neighbourhoods} within Model City
 * (e.g. a district or administrative area).
 * <p>
 * Relationship: one zone contains many neighbourhoods (1:N).
 */
@Entity
@Table(name = "zones")
@Getter
@Setter
@NoArgsConstructor
public class Zone {

    /** Surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable label shown in the UI (e.g. {@code "North District"}). */
    @Column(name = "display_name", length = 255, nullable = false)
    private String displayName;

    /**
     * Unique internal identifier (kebab-case, no spaces, no accents).
     * Used by the application logic to reference a zone programmatically
     * (e.g. {@code "north-district"}).
     */
    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    /**
     * Neighbourhoods belonging to this zone.
     * Loaded lazily — only fetched when explicitly accessed.
     */
    @OneToMany(mappedBy = "zone", fetch = FetchType.LAZY)
    private List<Neighbourhood> neighbourhoods = new ArrayList<>();

    /** Non-default-locale translations of {@link #displayName}, keyed by language code. */
    @ElementCollection
    @CollectionTable(name = "zone_translations", joinColumns = @JoinColumn(name = "zone_id"))
    @MapKeyColumn(name = "locale")
    @Column(name = "display_name")
    @BatchSize(size = 64)
    private Map<String, String> displayNameTranslations = new HashMap<>();
}

