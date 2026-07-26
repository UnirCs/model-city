package com.modelcity.engagement.questions.store.model;

import java.util.Map;

/** Read-only view of a civic question objective, exposed by the persistence adapter. */
public interface ObjectiveView {
    Long getId();
    String getObjective();
    int getSortOrder();

    /** Per-locale translations of the objective text, keyed by language code. */
    Map<String, ? extends Translation> getTranslations();

    /** Localizable fields of an objective for a single non-default locale. */
    interface Translation {
        String getObjective();
    }
}
