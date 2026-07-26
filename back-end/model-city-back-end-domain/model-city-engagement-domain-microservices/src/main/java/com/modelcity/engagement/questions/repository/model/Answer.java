package com.modelcity.engagement.questions.repository.model;

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
 * Microservices flavour of the citizen answer: adds the {@code question} back-reference to this topology's
 * (concrete) {@code CivicQuestion}, on top of the invariant mapping in {@link AnswerBase}. No shadow
 * navigation to {@code User} exists in this persistence unit.
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
}
