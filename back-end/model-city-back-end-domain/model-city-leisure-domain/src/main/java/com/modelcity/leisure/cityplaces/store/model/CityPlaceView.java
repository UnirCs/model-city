package com.modelcity.leisure.cityplaces.store.model;

import java.util.Map;

/** Read-only view of a city place, exposed by the persistence adapter. */
public interface CityPlaceView {
    Long getId();
    String getName();
    Double getLatitude();
    Double getLongitude();
    String getDescription();
    String getAddress();
    String getPhotoUrl1();
    String getPhotoUrl2();
    String getPhotoUrl3();
    String getAccessInfo();
    String getAccessibilityInfo();
    String getCategory();
    Integer getVisitDurationMinutes();

    /** Per-locale translations of the localizable fields, keyed by language code (e.g. {@code en}). */
    Map<String, ? extends Translation> getTranslations();

    /** Localizable fields of a city place for a single non-default locale. */
    interface Translation {
        String getName();
        String getDescription();
        String getAddress();
        String getAccessInfo();
        String getAccessibilityInfo();
    }
}
