package com.modelcity.engagement.questions.repository;

import com.modelcity.engagement.questions.repository.model.Answer;

/**
 * Concrete Spring Data repository binding {@link AnswerRepository} to this topology's {@code Answer}.
 */
public interface DefaultAnswerRepository extends AnswerRepository<Answer> {
}
