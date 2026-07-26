package com.modelcity.engagement.trails.repository;

import com.modelcity.engagement.trails.repository.model.EngagementTrailBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.UUID;

/**
 * Generic over the concrete {@link EngagementTrailBase} subclass so both topology libraries — and any
 * city that declares its own entity extending {@code EngagementTrailBase} — reuse this contract
 * instead of forking it. Marked {@code @NoRepositoryBean}: each topology exposes the platform default
 * through its own {@code DefaultEngagementTrailRepository}, binding {@code T} to its local
 * {@code EngagementTrail} entity.
 */
@NoRepositoryBean
public interface EngagementTrailRepository<T extends EngagementTrailBase>
        extends JpaRepository<T, UUID>, JpaSpecificationExecutor<T> {
}
