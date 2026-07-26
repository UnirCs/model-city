package com.modelcity.leisure.cityroutes.controller;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.web.bind.annotation.RestController;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceDto;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceSummaryDto;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteDto;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteRequestDto;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteSummaryDto;
import com.modelcity.leisure.cityroutes.usecase.CreateCityRouteUseCase;
import com.modelcity.leisure.cityroutes.usecase.DeleteCityRouteUseCase;
import com.modelcity.leisure.cityroutes.usecase.GetCityRouteForEditUseCase;
import com.modelcity.leisure.cityroutes.usecase.GetCityRoutePlaceUseCase;
import com.modelcity.leisure.cityroutes.usecase.GetCityRoutePlacesUseCase;
import com.modelcity.leisure.cityroutes.usecase.GetCityRouteUseCase;
import com.modelcity.leisure.cityroutes.usecase.GetCityRoutesUseCase;
import com.modelcity.leisure.cityroutes.usecase.UpdateCityRouteUseCase;

/**
 * Default concrete {@link CityRouteController}, bound to the platform DTOs. The component-scanned platform
 * default; disabled at startup when a local deployment provides its own bean for the seam.
 */
@RestController
@ModelCityDisabledIfInherited
public class DefaultCityRouteController extends CityRouteController<
        CityRouteDto, CityRouteSummaryDto, CityRouteRequestDto, CityPlaceDto, CityPlaceSummaryDto> {

    public DefaultCityRouteController(
            GetCityRoutesUseCase<CityRouteSummaryDto> getCityRoutesUseCase,
            GetCityRouteUseCase<CityRouteDto> getCityRouteUseCase,
            GetCityRouteForEditUseCase<CityRouteDto> getCityRouteForEditUseCase,
            GetCityRoutePlacesUseCase<CityPlaceSummaryDto> getCityRoutePlacesUseCase,
            GetCityRoutePlaceUseCase<CityPlaceDto> getCityRoutePlaceUseCase,
            CreateCityRouteUseCase<CityRouteDto, CityRouteRequestDto> createCityRouteUseCase,
            UpdateCityRouteUseCase<CityRouteDto, CityRouteRequestDto> updateCityRouteUseCase,
            DeleteCityRouteUseCase deleteCityRouteUseCase) {
        super(getCityRoutesUseCase, getCityRouteUseCase, getCityRouteForEditUseCase, getCityRoutePlacesUseCase,
                getCityRoutePlaceUseCase, createCityRouteUseCase, updateCityRouteUseCase, deleteCityRouteUseCase);
    }
}
