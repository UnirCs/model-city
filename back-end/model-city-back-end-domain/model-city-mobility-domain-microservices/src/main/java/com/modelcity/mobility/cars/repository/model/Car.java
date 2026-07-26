package com.modelcity.mobility.cars.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Microservices flavour of the car: the owner is referenced softly via the {@code ownerSub} column
 * inherited from {@link CarBase}; there is no JPA relationship because the mobility microservice's
 * persistence unit has no {@code User} entity. The whole mapping is inherited.
 */
@Entity
@Table(name = "cars")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Car extends CarBase {
}
