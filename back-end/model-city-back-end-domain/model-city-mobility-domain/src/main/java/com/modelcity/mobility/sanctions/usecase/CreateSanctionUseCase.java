package com.modelcity.mobility.sanctions.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.mobility.sanctions.controller.model.SanctionDto;
import com.modelcity.mobility.sanctions.controller.model.SanctionRequestDto;

/**
 * Records a new sanction issued by an operator or admin.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultCreateSanctionUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface CreateSanctionUseCase<T extends SanctionDto, R extends SanctionRequestDto> {

    T execute(String agentSub, R request);
}
