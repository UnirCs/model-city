package com.modelcity.leisure.publicspaces.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.publicspaces.controller.model.ReservationDto;
import com.modelcity.leisure.publicspaces.controller.model.ReservationRequestDto;

/**
 * Creates a citizen reservation on a reservable resource with window and overlap validations.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultCreateReservationUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface CreateReservationUseCase<T extends ReservationDto, R extends ReservationRequestDto> {

    T execute(Long publicSpaceId, Long resourceId, String citizenSub, R request);
}
