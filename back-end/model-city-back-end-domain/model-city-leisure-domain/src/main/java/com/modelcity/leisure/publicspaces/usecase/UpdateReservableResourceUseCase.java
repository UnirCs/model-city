package com.modelcity.leisure.publicspaces.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceDto;
import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceRequestDto;

/**
 * Updates an existing reservable resource. Operator or admin.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultUpdateReservableResourceUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface UpdateReservableResourceUseCase<T extends ReservableResourceDto, R extends ReservableResourceRequestDto> {

    T execute(Long publicSpaceId, Long resourceId, String sub,
              R request, String locale);
}
