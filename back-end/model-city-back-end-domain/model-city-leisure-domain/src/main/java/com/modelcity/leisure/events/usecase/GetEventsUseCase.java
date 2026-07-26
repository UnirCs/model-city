package com.modelcity.leisure.events.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.events.controller.model.EventSummaryDto;
import com.modelcity.leisure.events.repository.model.EventType;
import org.springframework.data.domain.Page;

/**
 * Returns the paginated list of active events filtered by optional type and paid flag.
 *
 * <p>Extension point: a local deployment overrides the default behaviour by declaring its own
 * {@code @Service} bean implementing this interface; the {@link DefaultGetEventsUseCase} default
 * then backs off (see {@code LeisureEventsAutoConfiguration}).
 */
@ModelCityExtensionPoint
public interface GetEventsUseCase<T extends EventSummaryDto> {

    Page<T> execute(EventType type, Boolean paid, int page, String locale);
}
