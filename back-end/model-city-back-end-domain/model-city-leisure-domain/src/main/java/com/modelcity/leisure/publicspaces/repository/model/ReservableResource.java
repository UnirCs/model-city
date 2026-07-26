package com.modelcity.leisure.publicspaces.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * A reservable unit (football pitch, padel court...) hosted by a {@link PublicSpace}. Platform default
 * entity: all columns live in {@link ReservableResourceBase}. A city that needs extra columns declares its
 * own {@code @Entity} extending {@link ReservableResourceBase} instead of editing this class.
 */
@Entity
@Table(name = "reservable_resources")
@SuperBuilder
@NoArgsConstructor
public class ReservableResource extends ReservableResourceBase {
}
