package com.modelcity.leisure.cityplaces.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * A point of interest within the city (monument, park, museum, square...). Platform default entity: all
 * columns live in {@link CityPlaceBase}. A city that needs extra columns declares its own {@code @Entity}
 * extending {@link CityPlaceBase} instead of editing this class (see {@code CityPlaceRepository}'s Javadoc).
 */
@Entity
@Table(name = "city_places")
@SuperBuilder
@NoArgsConstructor
public class CityPlace extends CityPlaceBase {
}
