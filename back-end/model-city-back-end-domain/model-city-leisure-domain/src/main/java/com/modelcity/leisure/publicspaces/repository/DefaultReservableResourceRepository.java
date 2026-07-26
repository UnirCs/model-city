package com.modelcity.leisure.publicspaces.repository;

import com.modelcity.leisure.publicspaces.repository.model.ReservableResource;

/**
 * Concrete Spring Data repository binding {@link ReservableResourceRepository} to the platform's
 * {@code ReservableResource}.
 */
public interface DefaultReservableResourceRepository extends ReservableResourceRepository<ReservableResource> {
}
