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
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

/** Default {@link UpdatePublicQuestionUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultUpdatePublicQuestionUseCase implements UpdatePublicQuestionUseCase<CivicQuestionDetailDto, CivicQuestionRequestDto> {

    private final CivicQuestionStore<? extends CivicQuestionView, CivicQuestionRequestDto> civicQuestionStore;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.PUBLIC_QUESTION, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.PUBLIC_QUESTIONS, allEntries = true)
    })
    public CivicQuestionDetailDto execute(Long id, String sub, CivicQuestionRequestDto request, String locale) {
        CivicQuestionView question = civicQuestionStore.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));

        if (!question.getOpenDate().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only future questions can be updated");
        }

        QuestionDateValidator.validateDates(request.getOpenDate(), request.getCloseDate());
        CivicQuestionView saved = civicQuestionStore.update(id, request);
        systemEventGenerator.civicQuestionUpdated(sub, saved, "PUT");
        log.info("Civic question id={} updated (PUT) by sub={}", id, sub);
        return CivicQuestionDetailDto.from(saved, 0, 0, locale);
    }
}
