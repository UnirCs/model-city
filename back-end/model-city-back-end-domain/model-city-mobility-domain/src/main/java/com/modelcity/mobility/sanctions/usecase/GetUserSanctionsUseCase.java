package com.modelcity.mobility.sanctions.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.mobility.sanctions.controller.model.SanctionSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Lists the sanctions associated to the caller's own cars (without the evidence image).
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetUserSanctionsUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetUserSanctionsUseCase<T extends SanctionSummaryDto> {

    Page<T> execute(String userId, String sub, Pageable pageable);
}
