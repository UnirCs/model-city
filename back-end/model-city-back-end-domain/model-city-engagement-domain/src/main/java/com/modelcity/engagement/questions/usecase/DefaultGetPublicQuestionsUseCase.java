package com.modelcity.engagement.questions.usecase;

import com.modelcity.engagement.questions.controller.model.CivicQuestionRequestDto;
import com.modelcity.engagement.questions.controller.model.CivicQuestionSummaryDto;
import com.modelcity.engagement.questions.store.CivicQuestionStore;
import com.modelcity.engagement.questions.store.model.CivicQuestionView;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link GetPublicQuestionsUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetPublicQuestionsUseCase implements GetPublicQuestionsUseCase<CivicQuestionSummaryDto> {

    private static final int PAGE_SIZE = 3;

    private final CivicQuestionStore<? extends CivicQuestionView, CivicQuestionRequestDto> civicQuestionStore;

    @Override
    @Cacheable(cacheNames = CacheNames.PUBLIC_QUESTIONS,
            key = "#locale + '-' + #status + '-' + #zoneId + '-' + #neighbourhoodId + '-' + #page")
    @Transactional(readOnly = true)
    public Page<CivicQuestionSummaryDto> execute(String status, Long zoneId, Long neighbourhoodId, int page, String locale) {
        return civicQuestionStore
                .search(status, zoneId, neighbourhoodId, PageRequest.of(page, PAGE_SIZE))
                .map(q -> CivicQuestionSummaryDto.from(q, locale));
    }
}
