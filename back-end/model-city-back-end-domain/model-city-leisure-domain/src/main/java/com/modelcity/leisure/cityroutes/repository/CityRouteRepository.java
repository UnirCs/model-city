package com.modelcity.leisure.cityroutes.repository;

import com.modelcity.leisure.cityroutes.repository.model.CityRouteBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Generic over the concrete {@link CityRouteBase} subclass so a city that declares its own entity (extending
 * {@code CityRouteBase} with extra columns) can reuse this contract instead of forking it. Marked
 * {@code @NoRepositoryBean}: Spring Data cannot proxy an unbound generic repository, so the platform default
 * is exposed through {@link DefaultCityRouteRepository}. A city binds its own subtype the same way:
 * {@code interface MyCityRouteRepository extends CityRouteRepository<MyCityRoute> {}}.
 */
@NoRepositoryBean
public interface CityRouteRepository<T extends CityRouteBase> extends JpaRepository<T, Long> {
}

