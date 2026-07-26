package com.modelcity.engagement.questions.store;

import com.modelcity.engagement.questions.repository.AnswerRepository;
import com.modelcity.engagement.questions.repository.CivicQuestionRepository;
import com.modelcity.engagement.questions.repository.model.Answer;
import com.modelcity.engagement.questions.repository.model.CivicQuestion;
import com.modelcity.engagement.questions.repository.model.Vote;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/** JPA adapter for citizen answers; the default {@link AnswerStore} bean. */
@Component
@ModelCityDisabledIfInherited
@RequiredArgsConstructor
public class DefaultAnswerStore implements AnswerStore {

    private final AnswerRepository<Answer> answerRepository;
    private final CivicQuestionRepository<CivicQuestion> civicQuestionRepository;

    @Override
    public boolean hasAnswered(Long questionId, String citizenId) {
        return answerRepository.existsByQuestionIdAndCitizenId(questionId, citizenId);
    }

    @Override
    public boolean hasVoted(Long questionId, String dniHash) {
        return answerRepository.existsByQuestionIdAndDniHash(questionId, dniHash);
    }

    @Override
    public Long createAnswer(Long questionId, String citizenSub, String dniHash, Vote vote) {
        Answer answer = Answer.builder()
                .question(civicQuestionRepository.getReferenceById(questionId))
                .citizenId(citizenSub)
                .dniHash(dniHash)
                .vote(vote)
                .answeredAt(OffsetDateTime.now())
                .build();
        Long id = answerRepository.save(answer).getId();

        if (vote == Vote.YES) {
            civicQuestionRepository.incrementYesCount(questionId);
        } else {
            civicQuestionRepository.incrementNoCount(questionId);
        }
        return id;
    }
}
