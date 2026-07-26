package com.modelcity.mobility.cars.store.model;

import java.time.OffsetDateTime;

/** Read-only view of a car, exposed by the persistence adapter. */
public interface CarView {
    Long getId();
    String getOwnerSub();
    String getLicensePlate();
    String getNickname();
    String getBrand();
    String getModel();
    OffsetDateTime getCreatedAt();
}
