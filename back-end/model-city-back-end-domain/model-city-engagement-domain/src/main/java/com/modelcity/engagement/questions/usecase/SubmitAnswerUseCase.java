package com.modelcity.engagement.questions.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.engagement.questions.controller.model.AnswerResponseDto;
import com.modelcity.engagement.questions.controller.model.SubmitAnswerRequestDto;

/**
 * Registers a citizen YES/NO answer after validating the operation authorization.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultSubmitAnswerUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface SubmitAnswerUseCase<A extends AnswerResponseDto, R extends SubmitAnswerRequestDto> {

    A execute(Long questionId, String sub, R request);
}
