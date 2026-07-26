package com.modelcity.leisure.events.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.events.controller.model.EventDto;

/**
 * Returns the detail of a single active event, flagging whether the requesting citizen already holds a ticket.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetEventUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetEventUseCase<T extends EventDto> {

    T execute(Long id, String sub, String locale);
}
