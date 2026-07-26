package com.modelcity.leisure.cityplaces.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;import com.modelcity.leisure.cityplaces.store.model.CityPlaceView;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceRequestDto;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceDto;
import com.modelcity.leisure.cityplaces.store.CityPlaceStore;
import lombok.RequiredArgsConstructor;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link GetCityPlaceUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetCityPlaceUseCase implements GetCityPlaceUseCase<CityPlaceDto> {

    private final CityPlaceStore<? extends CityPlaceView, CityPlaceRequestDto> cityPlaceStore;

    @Override
    @Cacheable(cacheNames = CacheNames.CITY_PLACE, key = "#locale + '-' + #id")
    @Transactional(readOnly = true)
    public CityPlaceDto execute(Long id, String locale) {
        return cityPlaceStore.findById(id)
                .map(p -> CityPlaceDto.from(p, locale))
                .orElseThrow(() -> new ResourceNotFoundException("CityPlace", id));
    }
}
