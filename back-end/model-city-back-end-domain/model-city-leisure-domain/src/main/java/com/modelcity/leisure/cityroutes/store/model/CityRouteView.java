package com.modelcity.leisure.cityroutes.store.model;

import java.util.List;
import java.util.Map;

/** Read-only view of a city route, exposed by the persistence adapter. */
public interface CityRouteView {
    Long getId();
    String getName();
    String getDescription();
    String getTargetAudience();
    String getImageUrl();
    Integer getEstimatedDurationMinutes();
    List<? extends CityRoutePlaceView> getRoutePlaces();

    /** Per-locale translations of the localizable fields, keyed by language code. */
    Map<String, ? extends Translation> getTranslations();

    /** Localizable fields of a city route for a single non-default locale. */
    interface Translation {
        String getName();
        String getDescription();
    }
}
