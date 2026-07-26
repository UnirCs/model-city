package com.modelcity.engagement.trails.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Microservices flavour of the engagement audit-log entry: soft id references only, inherited from
 * {@link EngagementTrailBase}; no {@code User}/{@code Neighbourhood}/{@code Zone} entities exist in
 * this persistence unit.
 */
@Entity
@Table(name = "engagement_trails")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class EngagementTrail extends EngagementTrailBase {
}
