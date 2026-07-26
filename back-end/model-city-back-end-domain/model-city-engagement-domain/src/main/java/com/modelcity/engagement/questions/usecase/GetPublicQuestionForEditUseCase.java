package com.modelcity.engagement.questions.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.engagement.questions.controller.model.CivicQuestionDetailDto;

/**
 * Returns the detail of a civic question with every locale of each localizable field (admin editing).
 * Not cached, no submittable flag.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetPublicQuestionForEditUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetPublicQuestionForEditUseCase<T extends CivicQuestionDetailDto> {

    T execute(Long id, String locale);
}
