package com.modelcity.leisure.cityplaces.usecase;


import com.modelcity.common.extensibility.ModelCityExtensionPoint;
/**
 * Deletes a city place by id.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultDeleteCityPlaceUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface DeleteCityPlaceUseCase {

    void execute(Long id, String sub);
}
