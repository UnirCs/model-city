package com.modelcity.engagement.questions.repository.model;

import com.modelcity.engagement.questions.store.model.CivicQuestionView;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Microservices flavour of the civic question: zone/neighbourhood are plain writable {@code Long} columns
 * (soft references — the zones live in another service); {@code objectives}/{@code answers} are declared here
 * (not on {@link CivicQuestionBase}) since {@link Objective}/{@link Answer} are themselves topology-specific
 * entities that must reference this concrete class.
 */
@Entity
@Table(name = "civic_questions")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CivicQuestion extends CivicQuestionBase implements CivicQuestionView {

    @Column(name = "zone_id", nullable = false)
    private Long zoneId;

    @Column(name = "neighbourhood_id", nullable = false)
    private Long neighbourhoodId;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<Objective> objectives = new ArrayList<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Answer> answers = new ArrayList<>();
}
