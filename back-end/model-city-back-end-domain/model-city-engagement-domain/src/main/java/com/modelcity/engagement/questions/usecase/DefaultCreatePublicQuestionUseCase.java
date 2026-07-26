package com.modelcity.engagement.questions.usecase;

import com.modelcity.engagement.questions.controller.model.CivicQuestionDetailDto;
import com.modelcity.engagement.questions.controller.model.CivicQuestionRequestDto;
import com.modelcity.engagement.questions.store.model.CivicQuestionView;
import com.modelcity.engagement.questions.store.CivicQuestionStore;
import com.modelcity.engagement.trails.SystemTrailGenerator;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link CreatePublicQuestionUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultCreatePublicQuestionUseCase implements CreatePublicQuestionUseCase<CivicQuestionDetailDto, CivicQuestionRequestDto> {

    private final CivicQuestionStore<? extends CivicQuestionView, CivicQuestionRequestDto> civicQuestionStore;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    @CacheEvict(cacheNames = CacheNames.PUBLIC_QUESTIONS, allEntries = true)
    public CivicQuestionDetailDto execute(String sub, CivicQuestionRequestDto request, String locale) {
        QuestionDateValidator.validateDates(request.getOpenDate(), request.getCloseDate());

        CivicQuestionView saved = civicQuestionStore.create(request);
        systemEventGenerator.civicQuestionCreated(sub, saved);
        log.info("Civic question created id={} by sub={}", saved.getId(), sub);
        return CivicQuestionDetailDto.from(saved, 0, 0, locale);
    }
}
