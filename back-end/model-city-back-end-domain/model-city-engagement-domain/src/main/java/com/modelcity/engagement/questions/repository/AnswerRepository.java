package com.modelcity.engagement.questions.repository;

import com.modelcity.engagement.questions.repository.model.AnswerBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Generic over the concrete {@link AnswerBase} subclass so both topology libraries — and any city that
 * declares its own entity extending {@code AnswerBase} — reuse this contract instead of forking it. Marked
 * {@code @NoRepositoryBean}: each topology exposes the platform default through its own
 * {@code DefaultAnswerRepository}, binding {@code T} to its local {@code Answer} entity.
 *
 * <p>The derived queries below navigate through the {@code question} property, which lives only on the
 * concrete subclass (see {@link AnswerBase}'s Javadoc) — this still resolves correctly because Spring Data
 * validates/derives the query against the JPA metamodel of the concrete bound entity class at
 * repository-bean-creation time, not via static reflection on this generic interface.
 */
@NoRepositoryBean
public interface AnswerRepository<T extends AnswerBase> extends JpaRepository<T, Long> {

    boolean existsByQuestionIdAndCitizenId(Long questionId, String citizenId);

    boolean existsByQuestionIdAndDniHash(Long questionId, String dniHash);
}
