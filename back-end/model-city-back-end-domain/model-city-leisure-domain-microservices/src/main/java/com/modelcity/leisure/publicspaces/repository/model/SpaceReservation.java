package com.modelcity.leisure.publicspaces.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** Microservices flavour of the space reservation: soft citizen reference (inherited citizenSub), no FK. */
@Entity
@Table(name = "space_reservations")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class SpaceReservation extends SpaceReservationBase {
}
