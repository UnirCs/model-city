package com.modelcity.mobility.trails.repository;

import com.modelcity.mobility.trails.repository.model.MobilityTrail;

/**
 * Concrete Spring Data repository binding {@link MobilityTrailRepository} to this topology's
 * {@code MobilityTrail}.
 */
public interface DefaultMobilityTrailRepository extends MobilityTrailRepository<MobilityTrail> {
}
