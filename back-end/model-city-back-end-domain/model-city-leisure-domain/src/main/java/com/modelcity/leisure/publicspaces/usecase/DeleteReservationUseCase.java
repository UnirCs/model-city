package com.modelcity.leisure.publicspaces.usecase;


import com.modelcity.common.extensibility.ModelCityExtensionPoint;
/**
 * Hard-deletes a reservation. Operator or admin.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultDeleteReservationUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface DeleteReservationUseCase {

    void execute(Long publicSpaceId, Long resourceId, Long reservationId, String sub);
}
