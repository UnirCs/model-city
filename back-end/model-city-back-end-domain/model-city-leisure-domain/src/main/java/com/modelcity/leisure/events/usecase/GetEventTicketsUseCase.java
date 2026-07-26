package com.modelcity.leisure.events.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.events.controller.model.TicketDto;
import org.springframework.data.domain.Page;

/**
 * Returns a paginated list of tickets for an event. Backoffice or admin.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetEventTicketsUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetEventTicketsUseCase<T extends TicketDto> {

    Page<T> execute(Long eventId, int page);
}
