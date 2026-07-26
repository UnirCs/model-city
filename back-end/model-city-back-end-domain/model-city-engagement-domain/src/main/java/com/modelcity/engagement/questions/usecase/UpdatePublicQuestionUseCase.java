package com.modelcity.engagement.questions.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.engagement.questions.controller.model.CivicQuestionDetailDto;
import com.modelcity.engagement.questions.controller.model.CivicQuestionRequestDto;

/**
 * Fully replaces a future civic question. Authorization enforced by {@code @ModelCityAccess}.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultUpdatePublicQuestionUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface UpdatePublicQuestionUseCase<T extends CivicQuestionDetailDto, R extends CivicQuestionRequestDto> {

    T execute(Long id, String sub, R request, String locale);
}
