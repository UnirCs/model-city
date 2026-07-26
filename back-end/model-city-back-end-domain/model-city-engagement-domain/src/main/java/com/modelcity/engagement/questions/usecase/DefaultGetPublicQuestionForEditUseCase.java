package com.modelcity.engagement.questions.usecase;

import com.modelcity.engagement.questions.controller.model.CivicQuestionDetailDto;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link GetPublicQuestionForEditUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetPublicQuestionForEditUseCase implements GetPublicQuestionForEditUseCase<CivicQuestionDetailDto> {

    private final CachedCivicQuestionReader cachedCivicQuestionReader;

    @Override
    @Transactional(readOnly = true)
    public CivicQuestionDetailDto execute(Long id, String locale) {
        return cachedCivicQuestionReader.getForEdit(id, locale);
    }
}
