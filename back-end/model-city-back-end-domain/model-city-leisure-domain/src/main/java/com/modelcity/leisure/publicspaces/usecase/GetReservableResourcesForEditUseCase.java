package com.modelcity.leisure.publicspaces.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * As {@link GetReservableResourcesUseCase} but each resource includes every locale of its localizable
 * fields (admin editing). Not cached.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetReservableResourcesForEditUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetReservableResourcesForEditUseCase<T extends ReservableResourceDto> {

    Page<T> execute(Long publicSpaceId, Pageable pageable, String locale);
}
