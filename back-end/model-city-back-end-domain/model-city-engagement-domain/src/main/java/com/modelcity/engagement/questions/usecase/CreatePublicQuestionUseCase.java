package com.modelcity.engagement.questions.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.engagement.questions.controller.model.CivicQuestionDetailDto;
import com.modelcity.engagement.questions.controller.model.CivicQuestionRequestDto;

/**
 * Creates a new civic question. Authorization enforced by {@code @ModelCityAccess.PlatformAdmin}.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultCreatePublicQuestionUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface CreatePublicQuestionUseCase<T extends CivicQuestionDetailDto, R extends CivicQuestionRequestDto> {

    T execute(String sub, R request, String locale);
}
