package com.modelcity.leisure.publicspaces.repository;

import com.modelcity.leisure.publicspaces.repository.model.SpaceReservation;

/**
 * Concrete Spring Data repository binding {@link SpaceReservationRepository} to this topology's
 * {@code SpaceReservation}.
 */
public interface DefaultSpaceReservationRepository extends SpaceReservationRepository<SpaceReservation> {
}
