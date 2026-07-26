package com.modelcity.mobility.reservations.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.mobility.reservations.controller.model.StreetReservationDto;
import com.modelcity.mobility.reservations.controller.model.StreetReservationRequestDto;

/**
 * Creates a new street reservation for the given user.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultCreateStreetReservationUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface CreateStreetReservationUseCase<T extends StreetReservationDto, R extends StreetReservationRequestDto> {

    T execute(String userId, String sub, R request);
}
