package com.modelcity.mobility.reservations.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.mobility.reservations.controller.model.StreetReservationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Returns active reservations and those from the last 30 days for the given user.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetUserStreetReservationsUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetUserStreetReservationsUseCase<T extends StreetReservationDto> {

    Page<T> execute(String userId, String sub, Pageable pageable);
}
