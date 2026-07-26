package com.modelcity.engagement.questions.repository.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.modelcity.engagement.questions.store.model.CivicQuestionView;

/**
 * Topology-invariant mapping for a civic question. Every scalar column is identical in the monolith and the
 * microservices, except the zone/neighbourhood reference: in the microservices flavour it is a plain
 * writable {@code Long} column, while in the monolith it is a real mandatory {@code @ManyToOne Zone}/
 * {@code Neighbourhood} with no underlying {@code Long} column at all — so there is no shared writable
 * column to keep here. The {@code objectives}/{@code answers} collections are also omitted: {@code Objective}
 * and {@code Answer} are their own entities and both need a {@code @ManyToOne} back-reference to the
 * concrete (topology-specific) {@code CivicQuestion}, which cannot be declared from this shared base.
 *
 * <p>This class intentionally does <em>not</em> implement {@code CivicQuestionView} — it cannot provide
 * {@code getZoneId()}/{@code getNeighbourhoodId()}/{@code getObjectives()} without depending on
 * topology-specific shapes/types. The concrete subclass in each topology library implements the view.
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class CivicQuestionBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Localizable fields below hold the default-locale (es) value; other locales live in {@link #translations}. */
    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Column(name = "open_date", nullable = false)
    private LocalDate openDate;

    @Column(name = "close_date", nullable = false)
    private LocalDate closeDate;

    /** Denormalised tally of YES votes, incremented atomically on each vote. */
    @Column(name = "yes_count", nullable = false)
    @Builder.Default
    private long yesCount = 0;

    /** Denormalised tally of NO votes, incremented atomically on each vote. */
    @Column(name = "no_count", nullable = false)
    @Builder.Default
    private long noCount = 0;

    /** Non-default-locale translations of the localizable fields, keyed by language code. */
    @ElementCollection
    @CollectionTable(name = "civic_question_translations", joinColumns = @JoinColumn(name = "question_id"))
    @MapKeyColumn(name = "locale")
    @BatchSize(size = 64)
    @Builder.Default
    private Map<String, CivicQuestionI18n> translations = new HashMap<>();

    /** Localizable fields of a civic question for a single locale. */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CivicQuestionI18n implements CivicQuestionView.Translation {
        @Column(name = "title")
        private String title;

        @Column(name = "description", columnDefinition = "TEXT")
        private String description;
    }
}
