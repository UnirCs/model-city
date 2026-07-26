package com.modelcity.leisure.publicspaces.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Returns the active reservable resources hosted by a public space.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetReservableResourcesUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetReservableResourcesUseCase<T extends ReservableResourceDto> {

    Page<T> execute(Long publicSpaceId, Pageable pageable, String locale);
}
