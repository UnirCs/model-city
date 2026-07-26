package com.modelcity.leisure.cityroutes.usecase;


import com.modelcity.common.extensibility.ModelCityExtensionPoint;
/**
 * Deletes a city route by id.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultDeleteCityRouteUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface DeleteCityRouteUseCase {

    void execute(Long id, String sub);
}
