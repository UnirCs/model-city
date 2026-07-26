package com.modelcity.engagement.trails.repository;

import com.modelcity.engagement.trails.repository.model.EngagementTrail;

/**
 * Concrete Spring Data repository binding {@link EngagementTrailRepository} to this topology's
 * {@code EngagementTrail}.
 */
public interface DefaultEngagementTrailRepository
        extends EngagementTrailRepository<EngagementTrail> {
}
