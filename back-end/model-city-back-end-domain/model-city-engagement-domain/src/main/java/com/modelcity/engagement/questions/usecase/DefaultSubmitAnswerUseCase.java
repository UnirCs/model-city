package com.modelcity.engagement.questions.usecase;

import com.modelcity.engagement.questions.controller.model.AnswerResponseDto;
import com.modelcity.engagement.questions.controller.model.CivicQuestionRequestDto;
import com.modelcity.engagement.questions.controller.model.SubmitAnswerRequestDto;
import com.modelcity.engagement.questions.repository.model.Vote;
import com.modelcity.engagement.questions.store.AnswerStore;
import com.modelcity.engagement.questions.store.CivicQuestionStore;
import com.modelcity.engagement.questions.store.model.CivicQuestionView;
import com.modelcity.engagement.trails.SystemTrailGenerator;
import com.modelcity.common.client.CoreClient;
import com.modelcity.common.dto.OperationAuthorizationResponseDto;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

/** Default {@link SubmitAnswerUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@Slf4j
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultSubmitAnswerUseCase implements SubmitAnswerUseCase<AnswerResponseDto, SubmitAnswerRequestDto> {

    private static final String RESOURCE_TYPE = "public-question";
    private static final String OPERATION_TYPE = "CONFIRM_ANSWER";
    private static final String STATUS_VERIFIED = "VERIFIED";

    private final CoreClient coreClient;
    private final CivicQuestionStore<? extends CivicQuestionView, CivicQuestionRequestDto> civicQuestionStore;
    private final AnswerStore answerStore;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    @CacheEvict(cacheNames = CacheNames.PUBLIC_QUESTION, allEntries = true)
    @Transactional
    public AnswerResponseDto execute(Long questionId, String sub, SubmitAnswerRequestDto request) {
        OperationAuthorizationResponseDto auth =
                coreClient.getOperationAuthorization(request.getOperationAuthorizationId());

        validateAuthorization(auth, questionId, sub);

        String dniHash = auth.dniHash();
        if (dniHash == null || dniHash.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "Authorization is not bound to a verified certificate");
        }

        CivicQuestionView question = civicQuestionStore.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));

        // Deduplicate by the verified DNI, not the account: two accounts sharing one DNI cannot
        // both vote. The (question_id, dni_hash) unique key is the backstop against races.
        if (answerStore.hasVoted(questionId, dniHash)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This citizen has already voted on this question");
        }

        Long answerId = answerStore.createAnswer(questionId, sub, dniHash, Vote.valueOf(request.getVote()));
        systemEventGenerator.answerSubmitted(sub, answerId, question, request.getVote());
        log.info("Answer id={} submitted for question={} by sub={}", answerId, questionId, sub);

        coreClient.burnOperationAuthorization(request.getOperationAuthorizationId());

        return new AnswerResponseDto(answerId);
    }

    private void validateAuthorization(OperationAuthorizationResponseDto auth, Long questionId, String sub) {
        if (!STATUS_VERIFIED.equals(auth.status())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "Operation authorization is not in VERIFIED status");
        }
        if (Instant.now().isAfter(auth.expiresAt())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Operation authorization has expired");
        }
        if (!RESOURCE_TYPE.equals(auth.resourceType())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "Authorization resource type mismatch");
        }
        if (!questionId.toString().equals(auth.resourceId())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "Authorization resource id does not match the question");
        }
        if (!sub.equals(auth.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Authorization belongs to a different user");
        }
        if (!OPERATION_TYPE.equals(auth.operationType())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "Authorization operation type mismatch");
        }
    }
}
