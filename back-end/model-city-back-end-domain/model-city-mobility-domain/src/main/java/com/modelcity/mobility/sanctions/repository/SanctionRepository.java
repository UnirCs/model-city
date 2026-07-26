package com.modelcity.mobility.sanctions.repository;

import com.modelcity.mobility.sanctions.repository.model.SanctionBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Generic over the concrete {@link SanctionBase} subclass so both topology libraries — and any city that
 * declares its own entity extending {@code SanctionBase} — reuse this contract instead of forking it.
 * Marked {@code @NoRepositoryBean}: each topology exposes the platform default through its own
 * {@code DefaultSanctionRepository}, binding {@code T} to its local {@code Sanction} entity.
 */
@NoRepositoryBean
public interface SanctionRepository<T extends SanctionBase>
        extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
}
