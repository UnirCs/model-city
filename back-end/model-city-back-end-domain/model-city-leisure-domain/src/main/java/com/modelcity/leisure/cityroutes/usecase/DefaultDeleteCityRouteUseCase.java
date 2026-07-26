package com.modelcity.leisure.cityroutes.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;import com.modelcity.leisure.cityroutes.controller.model.CityRouteRequestDto;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.cityroutes.store.CityRouteStore;
import com.modelcity.leisure.cityroutes.store.model.CityRouteView;
import com.modelcity.leisure.trails.SystemTrailGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link DeleteCityRouteUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultDeleteCityRouteUseCase implements DeleteCityRouteUseCase {

    private final CityRouteStore<? extends CityRouteView, CityRouteRequestDto> cityRouteStore;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.CITY_ROUTE, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.CITY_ROUTES, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.CITY_ROUTE_PLACES, allEntries = true)
    })
    public void execute(Long id, String sub) {
        CityRouteView route = cityRouteStore.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CityRoute", id));
        cityRouteStore.deleteById(id);
        systemEventGenerator.cityRouteDeleted(sub, route);
        log.info("CityRoute id={} deleted by sub={}", id, sub);
    }
}
