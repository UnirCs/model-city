package com.modelcity.leisure.cityplaces.repository;

import com.modelcity.leisure.cityplaces.repository.model.CityPlace;

/** Concrete Spring Data repository binding {@link CityPlaceRepository} to the platform's {@code CityPlace}. */
public interface DefaultCityPlaceRepository extends CityPlaceRepository<CityPlace> {
}
