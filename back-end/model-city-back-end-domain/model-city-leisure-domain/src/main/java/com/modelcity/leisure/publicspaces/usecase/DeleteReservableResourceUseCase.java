package com.modelcity.leisure.publicspaces.usecase;


import com.modelcity.common.extensibility.ModelCityExtensionPoint;
/**
 * Soft-deletes a reservable resource. Operator or admin.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultDeleteReservableResourceUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface DeleteReservableResourceUseCase {

    void execute(Long publicSpaceId, Long resourceId, String sub);
}
