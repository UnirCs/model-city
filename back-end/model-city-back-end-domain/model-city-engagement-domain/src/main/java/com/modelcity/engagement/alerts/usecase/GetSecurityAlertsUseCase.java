package com.modelcity.engagement.alerts.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.engagement.alerts.controller.model.SecurityAlertDto;
import org.springframework.data.domain.Page;

/**
 * Returns paginated non-expired security alerts ordered by severity then creation date.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetSecurityAlertsUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetSecurityAlertsUseCase<T extends SecurityAlertDto> {

    Page<T> execute(int page, String locale);
}
