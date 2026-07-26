package com.modelcity.leisure.events.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.events.controller.model.EventDto;
import com.modelcity.leisure.events.controller.model.EventRequestDto;

/**
 * Updates the editable fields of an active event. Backoffice or admin.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultUpdateEventUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface UpdateEventUseCase<T extends EventDto, R extends EventRequestDto> {

    T execute(Long id, String sub, R request, String locale);
}
