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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.HashMap;
import java.util.Map;

/**
 * Reference entity representing a neighbourhood in Model City.
 * <p>
 * {@code name} is the unique internal identifier used by the application
 * (e.g. {@code "el-recreo-norte"}), while {@code displayName} is the
 * human-readable label shown in the UI (e.g. {@code "El Recreo Norte"}).
 */
@Entity
@Table(name = "neighbourhoods")
@Getter
@Setter
@NoArgsConstructor
public class Neighbourhood {

    /** Surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable label shown in the UI. */
    @Column(name = "display_name", length = 255, nullable = false)
    private String displayName;

    /**
     * Unique internal identifier (kebab-case, no spaces, no accents).
     * Used by the application logic to reference a neighbourhood programmatically.
     */
    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    /**
     * Zone this neighbourhood belongs to. Mandatory.
     * Loaded lazily — use only when zone data is explicitly needed.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    /** Non-default-locale translations of {@link #displayName}, keyed by language code. */
    @ElementCollection
    @CollectionTable(name = "neighbourhood_translations", joinColumns = @JoinColumn(name = "neighbourhood_id"))
    @MapKeyColumn(name = "locale")
    @Column(name = "display_name")
    @BatchSize(size = 64)
    private Map<String, String> displayNameTranslations = new HashMap<>();
}
