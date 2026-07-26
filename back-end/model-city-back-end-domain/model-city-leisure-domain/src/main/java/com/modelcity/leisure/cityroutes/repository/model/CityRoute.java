package com.modelcity.leisure.cityroutes.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * A themed itinerary composed of several ordered {@link com.modelcity.leisure.cityplaces.repository.model.CityPlace}s.
 * Platform default entity: all columns live in {@link CityRouteBase}. A city that needs extra columns
 * declares its own {@code @Entity} extending {@link CityRouteBase} instead of editing this class.
 */
@Entity
@Table(name = "city_routes")
@SuperBuilder
@NoArgsConstructor
public class CityRoute extends CityRouteBase {
}
