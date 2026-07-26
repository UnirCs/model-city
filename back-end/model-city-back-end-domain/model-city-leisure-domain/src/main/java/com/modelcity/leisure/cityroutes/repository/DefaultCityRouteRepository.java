package com.modelcity.leisure.cityroutes.repository;

import com.modelcity.leisure.cityroutes.repository.model.CityRoute;

/** Concrete Spring Data repository binding {@link CityRouteRepository} to the platform's {@code CityRoute}. */
public interface DefaultCityRouteRepository extends CityRouteRepository<CityRoute> {
}
