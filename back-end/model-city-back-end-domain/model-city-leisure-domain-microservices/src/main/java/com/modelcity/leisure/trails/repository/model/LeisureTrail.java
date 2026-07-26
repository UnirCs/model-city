package com.modelcity.leisure.trails.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** Microservices flavour of the leisure audit-log entry: soft id references only, no FK navigations. */
@Entity
@Table(name = "leisure_trails")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class LeisureTrail extends LeisureTrailBase {
}
