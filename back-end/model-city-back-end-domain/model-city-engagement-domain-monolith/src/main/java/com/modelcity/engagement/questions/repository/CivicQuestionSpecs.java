package com.modelcity.engagement.questions.repository;

import com.modelcity.engagement.questions.repository.model.CivicQuestion;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * JPA Specifications for composable {@link CivicQuestion} filtering (monolith flavour: zone/neighbourhood
 * predicates navigate the real {@code zone}/{@code neighbourhood} associations).
 *
 * <p>Kept duplicated per-topology (not shared/generified like most other Specs classes in this codebase):
 * the microservices flavour of this class navigates the plain {@code zoneId}/{@code neighbourhoodId} columns
 * instead, which is a structurally different predicate, not just a different bound type.
 */
public final class CivicQuestionSpecs {

    private CivicQuestionSpecs() {}

    public static Specification<CivicQuestion> active(LocalDate today) {
        return (root, query, cb) -> cb.and(
                cb.lessThanOrEqualTo(root.get("openDate"), today),
                cb.greaterThanOrEqualTo(root.get("closeDate"), today)
        );
    }

    public static Specification<CivicQuestion> past(LocalDate today) {
        return (root, query, cb) -> cb.lessThan(root.get("closeDate"), today);
    }

    public static Specification<CivicQuestion> future(LocalDate today) {
        return (root, query, cb) -> cb.greaterThan(root.get("openDate"), today);
    }

    public static Specification<CivicQuestion> withZone(Long zoneId) {
        return (root, query, cb) -> cb.equal(root.get("zone").get("id"), zoneId);
    }

    public static Specification<CivicQuestion> withNeighbourhood(Long neighbourhoodId) {
        return (root, query, cb) -> cb.equal(root.get("neighbourhood").get("id"), neighbourhoodId);
    }
}
