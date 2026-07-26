package com.modelcity.mobility.reservations.repository;

import com.modelcity.mobility.reservations.repository.model.StreetReservation;

/**
 * Concrete Spring Data repository binding {@link StreetReservationRepository} to this topology's
 * {@code StreetReservation}.
 */
public interface DefaultStreetReservationRepository extends StreetReservationRepository<StreetReservation> {
}
