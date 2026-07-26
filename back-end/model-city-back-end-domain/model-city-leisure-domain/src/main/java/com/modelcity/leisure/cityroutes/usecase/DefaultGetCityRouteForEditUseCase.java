package com.modelcity.leisure.cityroutes.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;import com.modelcity.leisure.cityroutes.controller.model.CityRouteRequestDto;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteDto;
import com.modelcity.leisure.cityroutes.store.model.CityRouteView;
import com.modelcity.leisure.cityroutes.store.CityRouteStore;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link GetCityRouteForEditUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetCityRouteForEditUseCase implements GetCityRouteForEditUseCase<CityRouteDto> {

    private final CityRouteStore<? extends CityRouteView, CityRouteRequestDto> cityRouteStore;

    @Override
    @Transactional(readOnly = true)
    public CityRouteDto execute(Long id, String locale) {
        CityRouteView route = cityRouteStore.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CityRoute", id));
        return CityRouteDto.fromWithTranslations(route, locale);
    }
}
