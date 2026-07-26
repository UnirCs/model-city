package com.modelcity.leisure.trails.repository;

import com.modelcity.leisure.trails.repository.model.LeisureTrailBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.UUID;

/**
 * Generic over the concrete {@link LeisureTrailBase} subclass so both topology libraries — and any city that
 * declares its own entity extending {@code LeisureTrailBase} — reuse this contract instead of forking it.
 * Marked {@code @NoRepositoryBean}: Spring Data cannot proxy an unbound generic repository, so each topology
 * exposes the platform default through its own {@code DefaultLeisureTrailRepository}.
 */
@NoRepositoryBean
public interface LeisureTrailRepository<T extends LeisureTrailBase>
        extends JpaRepository<T, UUID>, JpaSpecificationExecutor<T> {
}
