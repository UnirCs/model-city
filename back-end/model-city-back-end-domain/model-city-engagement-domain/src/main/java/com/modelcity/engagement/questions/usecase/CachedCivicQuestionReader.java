package com.modelcity.engagement.questions.usecase;

import com.modelcity.engagement.questions.controller.model.CivicQuestionDetailDto;
import com.modelcity.engagement.questions.controller.model.CivicQuestionRequestDto;
import com.modelcity.engagement.questions.store.CivicQuestionStore;
import com.modelcity.engagement.questions.store.model.CivicQuestionView;
import lombok.RequiredArgsConstructor;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Caches the user-agnostic question detail (content + vote counts). The {@code submittable} flag is resolved per request. */
@Component
@RequiredArgsConstructor
public class CachedCivicQuestionReader {

    private final CivicQuestionStore<? extends CivicQuestionView, CivicQuestionRequestDto> civicQuestionStore;

    @Cacheable(cacheNames = CacheNames.PUBLIC_QUESTION, key = "#locale + '-' + #id")
    @Transactional(readOnly = true)
    public CivicQuestionDetailDto getById(Long id, String locale) {
        CivicQuestionView question = civicQuestionStore.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Civic question not found"));

        return CivicQuestionDetailDto.from(question, question.getYesCount(), question.getNoCount(), locale);
    }

    /** Detail including every locale of each localizable field (admin editing). Not cached. */
    @Transactional(readOnly = true)
    public CivicQuestionDetailDto getForEdit(Long id, String locale) {
        CivicQuestionView question = civicQuestionStore.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Civic question not found"));

        return CivicQuestionDetailDto.fromWithTranslations(question, question.getYesCount(), question.getNoCount(), locale);
    }
}
