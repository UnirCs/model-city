package com.modelcity.engagement.questions.repository;

import com.modelcity.engagement.questions.repository.model.CivicQuestion;

/**
 * Concrete Spring Data repository binding {@link CivicQuestionRepository} to this topology's
 * {@code CivicQuestion}.
 */
public interface DefaultCivicQuestionRepository extends CivicQuestionRepository<CivicQuestion> {
}
