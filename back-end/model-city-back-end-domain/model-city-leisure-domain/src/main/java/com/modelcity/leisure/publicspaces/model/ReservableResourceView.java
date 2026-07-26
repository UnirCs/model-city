package com.modelcity.leisure.publicspaces.model;

import java.util.Map;

/** Read-only view of a reservable resource, exposed by the persistence adapter. */
public interface ReservableResourceView {
    Long getId();
    Long getPublicSpaceId();
    String getName();
    String getDescription();
    String getResourceType();

    /** Per-locale translations of the localizable fields, keyed by language code. */
    Map<String, ? extends Translation> getTranslations();

    /** Localizable fields of a reservable resource for a single non-default locale. */
    interface Translation {
        String getName();
        String getDescription();
    }
}
