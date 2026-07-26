package com.modelcity.leisure.publicspaces.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.publicspaces.controller.model.ReservationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * Returns the reservations for a resource on a given date. Admin/operator callers see citizen
 * identification fields; everyone else only sees the time slot.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetReservationsUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetReservationsUseCase<T extends ReservationDto> {

    Page<T> execute(Long publicSpaceId, Long resourceId, LocalDate date, String callerSub, Pageable pageable);
}
