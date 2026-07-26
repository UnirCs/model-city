package com.modelcity.engagement.questions.usecase;

import com.modelcity.engagement.questions.controller.model.CivicQuestionDetailDto;
import com.modelcity.engagement.questions.store.AnswerStore;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link GetPublicQuestionUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetPublicQuestionUseCase implements GetPublicQuestionUseCase<CivicQuestionDetailDto> {

    private final CachedCivicQuestionReader cachedCivicQuestionReader;
    private final AnswerStore answerStore;

    @Override
    @Transactional(readOnly = true)
    public CivicQuestionDetailDto execute(Long id, String sub, String locale) {
        CivicQuestionDetailDto question = cachedCivicQuestionReader.getById(id, locale);
        boolean submittable = sub != null && !answerStore.hasAnswered(id, sub);
        return question.withSubmittable(submittable);
    }
}
