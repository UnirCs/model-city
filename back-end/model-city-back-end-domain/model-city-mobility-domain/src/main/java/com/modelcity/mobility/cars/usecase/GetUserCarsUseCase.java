package com.modelcity.mobility.cars.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.mobility.cars.controller.model.CarDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Lists all cars owned by a citizen. The caller sub must match the path id.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetUserCarsUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetUserCarsUseCase<T extends CarDto> {

    Page<T> execute(String userId, String sub, Pageable pageable);
}
