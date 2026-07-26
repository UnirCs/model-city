package com.modelcity.leisure.cityroutes.store;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;

import com.modelcity.leisure.cityroutes.controller.model.CityRouteRequestDto;
import com.modelcity.leisure.cityroutes.store.model.CityRouteView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/** Persistence port for city routes. */
@ModelCityExtensionPoint
public interface CityRouteStore<T extends CityRouteView, R extends CityRouteRequestDto> {

    Page<T> findAll(Pageable pageable);

    Optional<T> findById(Long id);

    /** Builds and persists a new route with its ordered places (validates the place ids). */
    T create(R request);

    /** Replaces the route fields and its ordered places (assumed to exist). */
    T update(Long id, R request);

    boolean existsById(Long id);

    void deleteById(Long id);
}
