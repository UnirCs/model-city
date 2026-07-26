package com.modelcity.leisure.cityplaces.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;import com.modelcity.leisure.cityplaces.store.model.CityPlaceView;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceRequestDto;

import com.modelcity.leisure.cityplaces.controller.model.CityPlaceDto;
import com.modelcity.leisure.cityplaces.store.CityPlaceStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link GetCityPlacesUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetCityPlacesUseCase implements GetCityPlacesUseCase<CityPlaceDto> {

    private static final int PAGE_SIZE = 6;

    private final CityPlaceStore<? extends CityPlaceView, CityPlaceRequestDto> cityPlaceStore;

    @Override
    @Cacheable(cacheNames = CacheNames.CITY_PLACES, key = "#locale + '-' + #category + '-' + #page")
    @Transactional(readOnly = true)
    public Page<CityPlaceDto> execute(String category, int page, String locale) {
        PageRequest pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
        if (category != null && !category.isBlank()) {
            return cityPlaceStore.findByCategory(category, pageable).map(p -> CityPlaceDto.from(p, locale));
        }
        return cityPlaceStore.findAll(pageable).map(p -> CityPlaceDto.from(p, locale));
    }
}
