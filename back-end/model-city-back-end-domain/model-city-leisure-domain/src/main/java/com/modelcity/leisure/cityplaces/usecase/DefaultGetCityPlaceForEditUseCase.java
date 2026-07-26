package com.modelcity.leisure.cityplaces.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;import com.modelcity.leisure.cityplaces.store.model.CityPlaceView;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceRequestDto;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceDto;
import com.modelcity.leisure.cityplaces.store.CityPlaceStore;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link GetCityPlaceForEditUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetCityPlaceForEditUseCase implements GetCityPlaceForEditUseCase<CityPlaceDto> {

    private final CityPlaceStore<? extends CityPlaceView, CityPlaceRequestDto> cityPlaceStore;

    @Override
    @Transactional(readOnly = true)
    public CityPlaceDto execute(Long id, String locale) {
        return cityPlaceStore.findById(id)
                .map(p -> CityPlaceDto.fromWithTranslations(p, locale))
                .orElseThrow(() -> new ResourceNotFoundException("CityPlace", id));
    }
}
