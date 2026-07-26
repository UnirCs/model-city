package com.modelcity.leisure.events.repository;

import com.modelcity.leisure.events.repository.model.EventRefundBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Generic over the concrete {@link EventRefundBase} subclass so both topology libraries — and any city that
 * declares its own entity extending {@code EventRefundBase} — reuse this contract instead of forking it.
 * Marked {@code @NoRepositoryBean}: Spring Data cannot proxy an unbound generic repository, so each topology
 * exposes the platform default through its own {@code DefaultEventRefundRepository}.
 */
@NoRepositoryBean
public interface EventRefundRepository<T extends EventRefundBase> extends JpaRepository<T, Long> {
}
