package com.modelcity.leisure.trails.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.common.trails.SystemTrailDto;
import com.modelcity.common.trails.SystemTrailQuery;
import org.springframework.data.domain.Page;

/**
 * Lists leisure system trails for platform admins, newest first.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetSystemTrailsUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetSystemTrailsUseCase {

    Page<SystemTrailDto> execute(SystemTrailQuery query, int page);
}
