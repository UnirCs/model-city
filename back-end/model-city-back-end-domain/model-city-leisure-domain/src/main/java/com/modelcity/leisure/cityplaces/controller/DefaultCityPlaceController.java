package com.modelcity.leisure.cityplaces.controller;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.web.bind.annotation.RestController;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceDto;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceRequestDto;
import com.modelcity.leisure.cityplaces.usecase.CreateCityPlaceUseCase;
import com.modelcity.leisure.cityplaces.usecase.DeleteCityPlaceUseCase;
import com.modelcity.leisure.cityplaces.usecase.GetCityPlaceForEditUseCase;
import com.modelcity.leisure.cityplaces.usecase.GetCityPlaceUseCase;
import com.modelcity.leisure.cityplaces.usecase.GetCityPlacesUseCase;
import com.modelcity.leisure.cityplaces.usecase.UpdateCityPlaceUseCase;

/**
 * Default concrete {@link CityPlaceController}, bound to the platform DTOs. The component-scanned platform
 * default; disabled at startup when a local deployment provides its own bean for the seam.
 */
@RestController
@ModelCityDisabledIfInherited
public class DefaultCityPlaceController extends CityPlaceController<CityPlaceDto, CityPlaceRequestDto> {

    public DefaultCityPlaceController(
            GetCityPlacesUseCase<CityPlaceDto> getCityPlacesUseCase,
            GetCityPlaceUseCase<CityPlaceDto> getCityPlaceUseCase,
            GetCityPlaceForEditUseCase<CityPlaceDto> getCityPlaceForEditUseCase,
            CreateCityPlaceUseCase<CityPlaceDto, CityPlaceRequestDto> createCityPlaceUseCase,
            UpdateCityPlaceUseCase<CityPlaceDto, CityPlaceRequestDto> updateCityPlaceUseCase,
            DeleteCityPlaceUseCase deleteCityPlaceUseCase) {
        super(getCityPlacesUseCase, getCityPlaceUseCase, getCityPlaceForEditUseCase,
                createCityPlaceUseCase, updateCityPlaceUseCase, deleteCityPlaceUseCase);
    }
}
