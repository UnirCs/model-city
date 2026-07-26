package com.modelcity.engagement.questions.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.engagement.questions.controller.model.CivicQuestionDetailDto;

/**
 * Returns the full detail of a single public civic question, flagging whether the citizen can still answer.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultGetPublicQuestionUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface GetPublicQuestionUseCase<T extends CivicQuestionDetailDto> {

    T execute(Long id, String sub, String locale);
}
