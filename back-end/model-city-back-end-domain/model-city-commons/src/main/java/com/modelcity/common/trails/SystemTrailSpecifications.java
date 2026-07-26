package com.modelcity.common.trails;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

/**
 * Reusable JPA specification for the system-trail read endpoints. The attribute names
 * ({@code eventType}, {@code responsibleUserId}, {@code occurredAt}) are identical across every
 * vertical's event entity, so a single generic specification serves all adapters.
 */
public final class SystemTrailSpecifications {

    private SystemTrailSpecifications() {}

    public static <T> Specification<T> matching(SystemTrailQuery q) {
        return (root, query, cb) -> {
            Predicate p = cb.conjunction();
            if (q.eventType() != null && !q.eventType().isBlank()) {
                p = cb.and(p, cb.equal(root.get("eventType"), q.eventType()));
            }
            if (q.responsibleUserId() != null && !q.responsibleUserId().isBlank()) {
                p = cb.and(p, cb.equal(root.get("responsibleUserId"), q.responsibleUserId()));
            }
            if (q.from() != null) {
                p = cb.and(p, cb.greaterThanOrEqualTo(root.get("occurredAt"), q.from()));
            }
            if (q.to() != null) {
                p = cb.and(p, cb.lessThanOrEqualTo(root.get("occurredAt"), q.to()));
            }
            return p;
        };
    }
}
