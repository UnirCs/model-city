package com.modelcity.mobility.cars.controller;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.web.bind.annotation.RestController;

import com.modelcity.mobility.cars.controller.model.CarDto;
import com.modelcity.mobility.cars.controller.model.CarRequestDto;
import com.modelcity.mobility.cars.usecase.CreateCarUseCase;
import com.modelcity.mobility.cars.usecase.GetUserCarsUseCase;

/**
 * Default concrete {@link UserCarController}. The component-scanned platform default; disabled at startup when a local
 * deployment provides its own bean for the seam.
 */
@RestController
@ModelCityDisabledIfInherited
public class DefaultUserCarController extends UserCarController<CarDto, CarRequestDto> {

    public DefaultUserCarController(CreateCarUseCase<CarDto, CarRequestDto> createCarUseCase,
                                    GetUserCarsUseCase<CarDto> getUserCarsUseCase) {
        super(createCarUseCase, getUserCarsUseCase);
    }
}
