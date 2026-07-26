package com.modelcity.mobility.trails.repository;

import com.modelcity.mobility.trails.repository.model.MobilityTrailBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.UUID;

/**
 * Generic over the concrete {@link MobilityTrailBase} subclass so both topology libraries — and any city
 * that declares its own entity extending {@code MobilityTrailBase} — reuse this contract instead of
 * forking it. Marked {@code @NoRepositoryBean}: each topology exposes the platform default through its own
 * {@code DefaultMobilityTrailRepository}, binding {@code T} to its local {@code MobilityTrail} entity.
 */
@NoRepositoryBean
public interface MobilityTrailRepository<T extends MobilityTrailBase>
        extends JpaRepository<T, UUID>, JpaSpecificationExecutor<T> {
}
