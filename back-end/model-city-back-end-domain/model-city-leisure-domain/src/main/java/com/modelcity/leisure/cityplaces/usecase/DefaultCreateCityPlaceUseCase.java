package com.modelcity.leisure.cityplaces.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceDto;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceRequestDto;
import com.modelcity.leisure.cityplaces.store.model.CityPlaceView;
import com.modelcity.leisure.cityplaces.store.CityPlaceStore;
import com.modelcity.leisure.trails.SystemTrailGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link CreateCityPlaceUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultCreateCityPlaceUseCase implements CreateCityPlaceUseCase<CityPlaceDto, CityPlaceRequestDto> {

    private final CityPlaceStore<? extends CityPlaceView, CityPlaceRequestDto> cityPlaceStore;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.CITY_PLACES, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.CITY_ROUTE_PLACES, allEntries = true)
    })
    public CityPlaceDto execute(String sub, CityPlaceRequestDto request, String locale) {
        CityPlaceView saved = cityPlaceStore.create(request);
        systemEventGenerator.cityPlaceCreated(sub, saved);
        log.info("CityPlace created id={} by sub={}", saved.getId(), sub);
        return CityPlaceDto.from(saved, locale);
    }
}
