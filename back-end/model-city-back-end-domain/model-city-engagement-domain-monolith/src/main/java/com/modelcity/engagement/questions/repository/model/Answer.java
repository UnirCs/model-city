package com.modelcity.engagement.questions.repository.model;

import com.modelcity.core.users.repository.model.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Monolith flavour of the citizen answer: adds the {@code question} back-reference to this topology's
 * (concrete) {@code CivicQuestion}, plus an additive shadow read-only {@code @ManyToOne} navigation to
 * {@link User} (the citizen that cast the vote), on top of the invariant mapping in {@link AnswerBase}.
 */
@Entity
@Table(
    name = "answers",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_answers_dni",
        columnNames = {"question_id", "dni_hash"}
    )
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Answer extends AnswerBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private CivicQuestion question;

    /** Read-only navigation to the citizen that cast the vote. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", insertable = false, updatable = false)
    private User citizen;
}
