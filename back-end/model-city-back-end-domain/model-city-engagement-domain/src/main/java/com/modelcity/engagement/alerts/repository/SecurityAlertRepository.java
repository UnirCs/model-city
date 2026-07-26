package com.modelcity.engagement.alerts.repository;

import com.modelcity.engagement.alerts.repository.model.SecurityAlertBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Generic over the concrete {@link SecurityAlertBase} subclass so both topology libraries — and any city
 * that declares its own entity extending {@code SecurityAlertBase} — reuse this contract instead of forking
 * it. Marked {@code @NoRepositoryBean}: each topology exposes the platform default through its own
 * {@code DefaultSecurityAlertRepository}, binding {@code T} to its local {@code SecurityAlert} entity.
 */
@NoRepositoryBean
public interface SecurityAlertRepository<T extends SecurityAlertBase> extends JpaRepository<T, Long> {

    /** Returns all alerts that have not yet expired, ordered by severity then creation date. */
    List<T> findByExpiresAtAfterOrderBySeverityAscCreatedAtDesc(OffsetDateTime now);

    /** Returns paginated alerts that have not yet expired, ordered by severity then creation date. */
    Page<T> findByExpiresAtAfterOrderBySeverityAscCreatedAtDesc(OffsetDateTime now, Pageable pageable);
}
