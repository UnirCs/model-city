package com.modelcity.engagement.questions.repository.model;

import com.modelcity.engagement.questions.store.model.ObjectiveView;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Topology-invariant mapping for a civic question objective. Unlike {@link CivicQuestionBase}, none of this
 * entity's own fields diverge by topology — only the {@code question} back-reference to the (topology
 * divergent) {@code CivicQuestion} does, so that {@code @ManyToOne} field is declared on the concrete
 * subclass in each topology library instead of here.
 *
 * <p>This class fully implements {@link ObjectiveView} since none of the view's members need the
 * {@code question} back-reference.
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class ObjectiveBase implements ObjectiveView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Holds the default-locale (es) value; other locales live in {@link #translations}. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String objective;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    /** Non-default-locale translations of the objective text, keyed by language code. */
    @ElementCollection
    @CollectionTable(name = "objective_translations", joinColumns = @JoinColumn(name = "objective_id"))
    @MapKeyColumn(name = "locale")
    @BatchSize(size = 64)
    @Builder.Default
    private Map<String, ObjectiveI18n> translations = new HashMap<>();

    /** Localizable fields of an objective for a single locale. */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ObjectiveI18n implements ObjectiveView.Translation {
        @Column(name = "objective", columnDefinition = "TEXT")
        private String objective;
    }
}
