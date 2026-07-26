package com.modelcity.engagement.questions.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.engagement.questions.controller.model.CivicQuestionDetailDto;

/**
 * Partially updates a civic question via JSON Merge Patch. Authorization enforced by {@code @ModelCityAccess}.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultPatchPublicQuestionUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface PatchPublicQuestionUseCase<T extends CivicQuestionDetailDto> {

    T execute(Long id, String sub, String patchBody, String locale);
}
