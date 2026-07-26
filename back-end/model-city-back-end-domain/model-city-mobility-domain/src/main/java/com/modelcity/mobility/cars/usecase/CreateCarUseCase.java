package com.modelcity.mobility.cars.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.mobility.cars.controller.model.CarDto;
import com.modelcity.mobility.cars.controller.model.CarRequestDto;

/**
 * Registers a new car for the given user. The user sub must match the path id.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultCreateCarUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface CreateCarUseCase<T extends CarDto, R extends CarRequestDto> {

    T execute(String userId, String sub, R request);
}
