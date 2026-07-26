package com.modelcity.mobility.reservations.store.model;

import com.modelcity.mobility.cars.store.model.CarView;
import com.modelcity.mobility.reservations.repository.model.ReservationStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Read-only view of a street reservation, exposed by the persistence adapter. */
public interface StreetReservationView {
    Long getId();
    String getUserSub();
    CarView getCar();
    Double getLatitude();
    Double getLongitude();
    OffsetDateTime getCreatedAt();
    OffsetDateTime getExpiresAt();
    Long getRenewedFromId();
    ReservationStatus getStatus();
    BigDecimal getPricePaid();
    String getCurrency();
    String getStripeCheckoutSessionId();
}
