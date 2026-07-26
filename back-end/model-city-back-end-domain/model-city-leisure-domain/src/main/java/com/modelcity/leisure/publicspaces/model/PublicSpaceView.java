package com.modelcity.leisure.publicspaces.model;

import java.util.Map;

/** Read-only view of a public space, exposed by the persistence adapter. */
public interface PublicSpaceView {
    Long getId();
    String getName();
    String getDescription();
    String getAddress();
    Double getLatitude();
    Double getLongitude();
    String getPhotoUrl1();
    String getPhotoUrl2();
    String getPhotoUrl3();

    /** Per-locale translations of the localizable fields, keyed by language code. */
    Map<String, ? extends Translation> getTranslations();

    /** Localizable fields of a public space for a single non-default locale. */
    interface Translation {
        String getName();
        String getDescription();
        String getAddress();
    }
}
