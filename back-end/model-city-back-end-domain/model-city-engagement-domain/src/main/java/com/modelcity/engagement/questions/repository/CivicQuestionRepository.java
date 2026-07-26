package com.modelcity.engagement.questions.repository;

import com.modelcity.engagement.questions.repository.model.CivicQuestionBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

/**
 * Generic over the concrete {@link CivicQuestionBase} subclass so both topology libraries — and any city
 * that declares its own entity extending {@code CivicQuestionBase} — reuse this contract instead of forking
 * it. Marked {@code @NoRepositoryBean}: each topology exposes the platform default through its own
 * {@code DefaultCivicQuestionRepository}, binding {@code T} to its local {@code CivicQuestion} entity.
 *
 * <p>The {@code @Query} bodies use {@code #{#entityName}} instead of a literal entity name so the same JPQL
 * resolves correctly against whichever concrete entity {@code T} is bound to.
 */
@NoRepositoryBean
public interface CivicQuestionRepository<T extends CivicQuestionBase>
        extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

    /** Atomically increments the YES tally without loading the entity. */
    @Modifying
    @Query("update #{#entityName} q set q.yesCount = q.yesCount + 1 where q.id = :id")
    void incrementYesCount(@Param("id") Long id);

    /** Atomically increments the NO tally without loading the entity. */
    @Modifying
    @Query("update #{#entityName} q set q.noCount = q.noCount + 1 where q.id = :id")
    void incrementNoCount(@Param("id") Long id);
}
