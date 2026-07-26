package com.modelcity.leisure.cityplaces.repository;

import com.modelcity.leisure.cityplaces.repository.model.CityPlaceBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Generic over the concrete {@link CityPlaceBase} subclass so a city that declares its own entity (extending
 * {@code CityPlaceBase} with extra columns) can reuse this contract instead of forking it — mirroring the
 * generics already carried by {@code CityPlaceStore}. Marked {@code @NoRepositoryBean}: Spring Data cannot
 * proxy an unbound generic repository, so the platform default is exposed through
 * {@link DefaultCityPlaceRepository}, which binds {@code T} to the platform's {@code CityPlace} entity. A city
 * binds its own subtype the same way: {@code interface MyCityPlaceRepository extends
 * CityPlaceRepository<MyCityPlace> {}}.
 */
@NoRepositoryBean
public interface CityPlaceRepository<T extends CityPlaceBase> extends JpaRepository<T, Long> {

    /** Finds all places whose category matches (case-insensitive). */
    Page<T> findByCategoryIgnoreCase(String category, Pageable pageable);
}

