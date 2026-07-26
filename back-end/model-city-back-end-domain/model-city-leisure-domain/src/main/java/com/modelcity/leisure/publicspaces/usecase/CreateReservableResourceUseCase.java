package com.modelcity.leisure.publicspaces.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceDto;
import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceRequestDto;

/**
 * Creates a new reservable resource under an existing public space. Operator or admin.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultCreateReservableResourceUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface CreateReservableResourceUseCase<T extends ReservableResourceDto, R extends ReservableResourceRequestDto> {

    T execute(Long publicSpaceId, String sub, R request, String locale);
}
