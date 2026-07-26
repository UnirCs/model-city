package com.modelcity.leisure.events.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.events.controller.model.EventDto;

/**
 * Returns the detail of a single event with every locale of each localizable field (admin editing).
 * Not cached, no acquired flag.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetEventForEditUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetEventForEditUseCase<T extends EventDto> {

    T execute(Long id, String locale);
}
