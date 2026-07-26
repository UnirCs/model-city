package com.modelcity.engagement.questions.store;

import com.modelcity.engagement.questions.repository.model.Vote;
import com.modelcity.common.extensibility.ModelCityExtensionPoint;

/** Persistence port for citizen answers to civic questions. */
@ModelCityExtensionPoint
public interface AnswerStore {

    /** True if the given account (Auth0 sub) already answered. Best-effort UI hint only. */
    boolean hasAnswered(Long questionId, String citizenId);

    /** True if the given verified DNI already voted. This is the authoritative deduplication check. */
    boolean hasVoted(Long questionId, String dniHash);

    /**
     * Persists a new vote for the given (existing) question and atomically increments the question's
     * tally counter. Deduplication is enforced by the {@code (question_id, dni_hash)} unique key.
     *
     * @return the id of the persisted vote
     */
    Long createAnswer(Long questionId, String citizenSub, String dniHash, Vote vote);
}
