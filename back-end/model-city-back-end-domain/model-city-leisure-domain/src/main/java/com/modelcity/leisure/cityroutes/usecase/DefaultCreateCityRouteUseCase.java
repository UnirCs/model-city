package com.modelcity.leisure.cityroutes.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteDto;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteRequestDto;
import com.modelcity.leisure.cityroutes.store.model.CityRouteView;
import com.modelcity.leisure.cityroutes.store.CityRouteStore;
import com.modelcity.leisure.trails.SystemTrailGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link CreateCityRouteUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultCreateCityRouteUseCase implements CreateCityRouteUseCase<CityRouteDto, CityRouteRequestDto> {

    private final CityRouteStore<? extends CityRouteView, CityRouteRequestDto> cityRouteStore;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    @CacheEvict(cacheNames = CacheNames.CITY_ROUTES, allEntries = true)
    public CityRouteDto execute(String sub, CityRouteRequestDto request, String locale) {
        CityRouteView saved = cityRouteStore.create(request);
        systemEventGenerator.cityRouteCreated(sub, saved);
        log.info("CityRoute created id={} by sub={}", saved.getId(), sub);
        return CityRouteDto.from(saved, locale);
    }
}
