package com.modelcity.mobility.sanctions.store.model;

import java.time.OffsetDateTime;

/** Read-only view of a sanction, exposed by the persistence adapter. */
public interface SanctionView {
    Long getId();
    String getLicensePlate();
    Double getLatitude();
    Double getLongitude();
    String getAgentSub();
    OffsetDateTime getCreatedAt();
    String getImageBase64();
}
