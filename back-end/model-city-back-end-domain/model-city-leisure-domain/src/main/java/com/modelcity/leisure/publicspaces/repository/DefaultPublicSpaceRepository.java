package com.modelcity.leisure.publicspaces.repository;

import com.modelcity.leisure.publicspaces.repository.model.PublicSpace;

/** Concrete Spring Data repository binding {@link PublicSpaceRepository} to the platform's {@code PublicSpace}. */
public interface DefaultPublicSpaceRepository extends PublicSpaceRepository<PublicSpace> {
}
