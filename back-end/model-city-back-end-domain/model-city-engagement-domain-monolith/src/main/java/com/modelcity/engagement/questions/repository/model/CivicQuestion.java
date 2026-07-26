package com.modelcity.engagement.questions.repository.model;

import com.modelcity.engagement.questions.store.model.CivicQuestionView;
import com.modelcity.core.users.repository.model.Neighbourhood;
import com.modelcity.core.users.repository.model.Zone;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Monolith flavour of the civic question: zone/neighbourhood are real, mandatory {@code @ManyToOne}
 * associations (no underlying {@code Long} column); {@code objectives}/{@code answers} are declared here
 * (not on {@link CivicQuestionBase}) since {@link Objective}/{@link Answer} are themselves topology-specific
 * entities that must reference this concrete class. {@code getZoneId()}/{@code getNeighbourhoodId()} are
 * derived, read-only conveniences exposed for DTO mapping.
 */
@Entity
@Table(name = "civic_questions")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CivicQuestion extends CivicQuestionBase implements CivicQuestionView {

    /** Zone the question belongs to. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    /** Neighbourhood within {@link #zone} the question targets. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "neighbourhood_id", nullable = false)
    private Neighbourhood neighbourhood;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<Objective> objectives = new ArrayList<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Answer> answers = new ArrayList<>();

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
