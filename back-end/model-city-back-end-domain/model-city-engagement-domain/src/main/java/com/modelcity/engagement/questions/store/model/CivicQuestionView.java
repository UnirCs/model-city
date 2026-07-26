package com.modelcity.engagement.questions.store.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Read-only view of a civic question, exposed by the persistence adapter so the domain
 * (controllers, use cases, DTOs) stays decoupled from the deployment-specific JPA entity.
 * Each deployment maps zone/neighbourhood differently (soft id vs FK) but exposes the same surface.
 */
public interface CivicQuestionView {
    Long getId();
    String getTitle();
    String getDescription();
    String getImageUrl();
    LocalDate getOpenDate();
    LocalDate getCloseDate();
    Long getZoneId();
    Long getNeighbourhoodId();
    List<? extends ObjectiveView> getObjectives();

    /** Denormalised tally of YES votes, kept in sync on each vote to avoid scanning the vote ledger. */
    long getYesCount();

    /** Denormalised tally of NO votes, kept in sync on each vote to avoid scanning the vote ledger. */
    long getNoCount();

    /** Per-locale translations of the localizable fields, keyed by language code. */
    Map<String, ? extends Translation> getTranslations();

    /** Localizable fields of a civic question for a single non-default locale. */
    interface Translation {
        String getTitle();
        String getDescription();
    }
}
