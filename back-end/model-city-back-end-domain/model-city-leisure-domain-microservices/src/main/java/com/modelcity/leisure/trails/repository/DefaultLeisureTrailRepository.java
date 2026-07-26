package com.modelcity.leisure.trails.repository;

import com.modelcity.leisure.trails.repository.model.LeisureTrail;

/** Concrete Spring Data repository binding {@link LeisureTrailRepository} to this topology's {@code LeisureTrail}. */
public interface DefaultLeisureTrailRepository extends LeisureTrailRepository<LeisureTrail> {
}
