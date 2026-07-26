package com.modelcity.leisure.publicspaces.model;

import java.time.LocalDate;
import java.time.LocalTime;

/** Read-only view of a space reservation, exposed by the persistence adapter. */
public interface SpaceReservationView {
    Long getId();
    Long getResourceId();
    String getCitizenSub();
    String getCitizenName();
    LocalDate getReservationDate();
    LocalTime getStartTime();
    LocalTime getEndTime();
}
