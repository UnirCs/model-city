package com.modelcity.engagement.questions.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.engagement.questions.controller.model.CivicQuestionSummaryDto;
import org.springframework.data.domain.Page;

/**
 * Returns a paginated, filtered list of public civic questions.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetPublicQuestionsUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetPublicQuestionsUseCase<T extends CivicQuestionSummaryDto> {

    Page<T> execute(String status, Long zoneId, Long neighbourhoodId, int page, String locale);
}
