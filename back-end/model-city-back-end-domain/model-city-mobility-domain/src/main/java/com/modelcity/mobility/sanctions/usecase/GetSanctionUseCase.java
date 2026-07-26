package com.modelcity.mobility.sanctions.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.mobility.sanctions.controller.model.SanctionDto;

/**
 * Returns a single sanction by id, with the evidence image. Admin/operator only.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetSanctionUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetSanctionUseCase<T extends SanctionDto> {

    T execute(Long id);
}
