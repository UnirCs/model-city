package com.modelcity.mobility.reservations.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.mobility.reservations.controller.model.StreetReservationDto;
import com.modelcity.mobility.reservations.controller.model.StreetReservationRequestDto;

/**
 * Renews an active reservation by creating a brand new one linked to the original.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultRenewStreetReservationUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface RenewStreetReservationUseCase<T extends StreetReservationDto, R extends StreetReservationRequestDto> {

    T execute(String userId, String sub, Long reservationId, R request);
}
