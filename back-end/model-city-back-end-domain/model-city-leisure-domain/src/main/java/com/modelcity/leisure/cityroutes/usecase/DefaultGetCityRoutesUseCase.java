package com.modelcity.leisure.cityroutes.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;import com.modelcity.leisure.cityroutes.store.model.CityRouteView;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteRequestDto;

import com.modelcity.leisure.cityroutes.controller.model.CityRouteSummaryDto;
import com.modelcity.leisure.cityroutes.store.CityRouteStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link GetCityRoutesUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetCityRoutesUseCase implements GetCityRoutesUseCase<CityRouteSummaryDto> {

    private static final int PAGE_SIZE = 3;

    private final CityRouteStore<? extends CityRouteView, CityRouteRequestDto> cityRouteStore;

    @Override
    @Cacheable(cacheNames = CacheNames.CITY_ROUTES, key = "#locale + '-' + #page")
    @Transactional(readOnly = true)
    public Page<CityRouteSummaryDto> execute(int page, String locale) {
        return cityRouteStore
                .findAll(PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending()))
                .map(r -> CityRouteSummaryDto.from(r, locale));
    }
}
