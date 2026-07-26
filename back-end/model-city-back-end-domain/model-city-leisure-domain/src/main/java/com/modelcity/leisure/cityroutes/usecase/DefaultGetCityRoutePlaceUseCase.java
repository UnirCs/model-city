package com.modelcity.leisure.cityroutes.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;import com.modelcity.leisure.cityroutes.controller.model.CityRouteRequestDto;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceDto;
import com.modelcity.leisure.cityplaces.store.model.CityPlaceView;
import com.modelcity.leisure.cityroutes.store.model.CityRouteView;
import com.modelcity.leisure.cityroutes.store.CityRouteStore;
import lombok.RequiredArgsConstructor;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link GetCityRoutePlaceUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetCityRoutePlaceUseCase implements GetCityRoutePlaceUseCase<CityPlaceDto> {

    private final CityRouteStore<? extends CityRouteView, CityRouteRequestDto> cityRouteStore;

    @Override
    @Cacheable(cacheNames = CacheNames.CITY_ROUTE_PLACES, key = "#locale + '-route-' + #routeId + '-place-' + #placeId")
    @Transactional(readOnly = true)
    public CityPlaceDto execute(Long routeId, Long placeId, String locale) {
        CityRouteView route = cityRouteStore.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("CityRoute", routeId));
        CityPlaceView place = route.getRoutePlaces().stream()
                .map(rp -> rp.getPlace())
                .filter(p -> p.getId().equals(placeId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CityPlace id=" + placeId + " in CityRoute", routeId));
        return CityPlaceDto.from(place, locale);
    }
}
