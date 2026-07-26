package com.modelcity.engagement.alerts.repository;

import com.modelcity.engagement.alerts.repository.model.SecurityAlert;

/**
 * Concrete Spring Data repository binding {@link SecurityAlertRepository} to this topology's
 * {@code SecurityAlert}.
 */
public interface DefaultSecurityAlertRepository extends SecurityAlertRepository<SecurityAlert> {
}
