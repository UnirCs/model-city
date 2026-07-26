package com.modelcity.leisure.events.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.events.controller.model.CitizenTicketDto;
import org.springframework.data.domain.Page;

/**
 * Returns the paginated list of tickets belonging to a citizen. A citizen may only query their own tickets;
 * admin and backoffice may query any user.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetCitizenTicketsUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetCitizenTicketsUseCase<T extends CitizenTicketDto> {

    Page<T> execute(String targetSub, String callerSub, int page, String period);
}
