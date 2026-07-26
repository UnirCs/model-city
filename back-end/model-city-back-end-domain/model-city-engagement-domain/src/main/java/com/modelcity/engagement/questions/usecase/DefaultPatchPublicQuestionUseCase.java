package com.modelcity.engagement.questions.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
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

/** Default {@link PatchPublicQuestionUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultPatchPublicQuestionUseCase implements PatchPublicQuestionUseCase<CivicQuestionDetailDto> {

    private final CivicQuestionStore<? extends CivicQuestionView, CivicQuestionRequestDto> civicQuestionStore;
    private final ObjectMapper objectMapper;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.PUBLIC_QUESTION, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.PUBLIC_QUESTIONS, allEntries = true)
    })
    public CivicQuestionDetailDto execute(Long id, String sub, String patchBody, String locale) {
        CivicQuestionView question = civicQuestionStore.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));

        CivicQuestionRequestDto merged = applyMergePatch(question, patchBody);
        CivicQuestionView saved = civicQuestionStore.update(id, merged);
        systemEventGenerator.civicQuestionUpdated(sub, saved, "PATCH");
        log.info("Civic question id={} patched by sub={}", id, sub);
        return CivicQuestionDetailDto.from(saved, 0, 0, locale);
    }

    private CivicQuestionRequestDto applyMergePatch(CivicQuestionView question, String patchBody) {
        try {
            JsonNode currentNode = objectMapper.valueToTree(CivicQuestionRequestDto.fromEntity(question));
            JsonNode patchNode = objectMapper.readTree(patchBody);
            JsonNode patched = JsonMergePatch.fromJson(patchNode).apply(currentNode);
            return objectMapper.treeToValue(patched, CivicQuestionRequestDto.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid merge patch: " + e.getMessage());
        }
    }
}
