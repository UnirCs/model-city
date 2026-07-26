package com.modelcity.leisure.events.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.events.controller.model.EventDto;
import com.modelcity.leisure.events.controller.model.EventRequestDto;

/**
 * Creates a new event. For paid events, a Stripe Product + Price is created first.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultCreateEventUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface CreateEventUseCase<T extends EventDto, R extends EventRequestDto> {

    T execute(String sub, R request, String locale);
}
