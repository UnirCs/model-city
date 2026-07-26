package com.modelcity.leisure.cityroutes.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;import com.modelcity.leisure.cityroutes.controller.model.CityRouteRequestDto;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceSummaryDto;
import com.modelcity.leisure.cityroutes.store.model.CityRouteView;
import com.modelcity.leisure.cityroutes.store.CityRouteStore;
import lombok.RequiredArgsConstructor;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Default {@link GetCityRoutePlacesUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetCityRoutePlacesUseCase implements GetCityRoutePlacesUseCase<CityPlaceSummaryDto> {

    private final CityRouteStore<? extends CityRouteView, CityRouteRequestDto> cityRouteStore;

    @Override
    @Cacheable(cacheNames = CacheNames.CITY_ROUTE_PLACES,
            key = "#locale + '-route-' + #routeId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<CityPlaceSummaryDto> execute(Long routeId, Pageable pageable, String locale) {
        CityRouteView route = cityRouteStore.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("CityRoute", routeId));
        List<CityPlaceSummaryDto> all = route.getRoutePlaces().stream()
                .map(rp -> CityPlaceSummaryDto.from(rp.getPlace(), locale))
                .toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        return new PageImpl<>(all.subList(start, end), pageable, all.size());
    }
}
