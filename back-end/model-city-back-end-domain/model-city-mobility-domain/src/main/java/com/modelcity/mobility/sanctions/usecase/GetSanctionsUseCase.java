package com.modelcity.mobility.sanctions.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.mobility.sanctions.controller.model.SanctionSummaryDto;
import org.springframework.data.domain.Page;

import java.time.OffsetDateTime;

/**
 * Lists sanctions for admins/operators (without image).
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetSanctionsUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetSanctionsUseCase<T extends SanctionSummaryDto> {

    Page<T> execute(String licensePlate, OffsetDateTime from, OffsetDateTime to, int page);
}
