package com.modelcity.mobility.reservations.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.mobility.reservations.controller.model.StreetReservationDto;
import org.springframework.data.domain.Page;

import java.time.OffsetDateTime;

/**
 * Lists street reservations for admins/operators with multiple optional filters.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetStreetReservationsUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetStreetReservationsUseCase<T extends StreetReservationDto> {

    Page<T> execute(String licensePlate, OffsetDateTime from, OffsetDateTime to, Boolean active, int page);
}
