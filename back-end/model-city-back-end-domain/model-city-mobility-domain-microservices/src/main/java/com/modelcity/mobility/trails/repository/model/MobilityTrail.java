package com.modelcity.mobility.trails.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Microservices flavour of the mobility audit-log entry: soft id references only, inherited from
 * {@link MobilityTrailBase}; no {@code User}/{@code Neighbourhood}/{@code Zone} entities exist in this
 * persistence unit.
 */
@Entity
@Table(name = "mobility_trails")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class MobilityTrail extends MobilityTrailBase {
}
