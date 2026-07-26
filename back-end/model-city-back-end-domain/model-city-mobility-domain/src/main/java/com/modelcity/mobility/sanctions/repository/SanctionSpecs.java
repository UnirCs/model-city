package com.modelcity.mobility.sanctions.repository;

import com.modelcity.mobility.sanctions.repository.model.SanctionBase;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Reusable JPA specifications for {@link SanctionBase} listings, generic over the concrete subclass so it
 * can be shared by both topology libraries instead of being duplicated per-topology. All predicates
 * navigate base (invariant) columns only, so no compile-time dependency on a concrete entity is needed.
 */
public final class SanctionSpecs {

    private SanctionSpecs() {}

    public static <T extends SanctionBase> Specification<T> licensePlateEquals(String plate) {
        if (plate == null || plate.isBlank()) return null;
        String normalized = plate.trim().toUpperCase();
        return (root, q, cb) -> cb.equal(cb.upper(root.get("licensePlate")), normalized);
    }

    public static <T extends SanctionBase> Specification<T> createdBetween(OffsetDateTime from, OffsetDateTime to) {
        if (from == null && to == null) return null;
        return (root, q, cb) -> {
            Predicate p = cb.conjunction();
            if (from != null) p = cb.and(p, cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            if (to != null)   p = cb.and(p, cb.lessThanOrEqualTo(root.get("createdAt"), to));
            return p;
        };
    }

    public static <T extends SanctionBase> Specification<T> licensePlateIn(Collection<String> plates) {
        if (plates == null || plates.isEmpty()) return null;
        List<String> normalized = plates.stream().map(p -> p.trim().toUpperCase()).toList();
        return (root, q, cb) -> cb.upper(root.get("licensePlate")).in(normalized);
    }
}
