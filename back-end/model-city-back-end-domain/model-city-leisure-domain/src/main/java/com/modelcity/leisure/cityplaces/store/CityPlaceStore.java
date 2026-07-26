package com.modelcity.leisure.cityplaces.store;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;

import com.modelcity.leisure.cityplaces.controller.model.CityPlaceRequestDto;
import com.modelcity.leisure.cityplaces.store.model.CityPlaceView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/** Persistence port for city places. */
@ModelCityExtensionPoint
public interface CityPlaceStore<T extends CityPlaceView, R extends CityPlaceRequestDto> {

    Page<T> findAll(Pageable pageable);

    Page<T> findByCategory(String category, Pageable pageable);

    Optional<T> findById(Long id);

    T create(R request);

    T update(Long id, R request);

    boolean existsById(Long id);

    void deleteById(Long id);
}
