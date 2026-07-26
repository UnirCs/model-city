package com.modelcity.mobility.sanctions.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Microservices flavour of the sanction: the issuing agent is referenced softly via the {@code agentSub}
 * column inherited from {@link SanctionBase}. The whole mapping is inherited.
 */
@Entity
@Table(name = "sanctions")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Sanction extends SanctionBase {
}
