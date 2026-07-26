package com.modelcity.engagement.questions.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Monolith flavour of the civic question objective: adds the {@code question} back-reference to this
 * topology's (concrete) {@code CivicQuestion}, on top of the invariant mapping in {@link ObjectiveBase}.
 */
@Entity
@Table(name = "objectives")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Objective extends ObjectiveBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private CivicQuestion question;
}
