package com.modelcity.mobility.reservations.repository;

import com.modelcity.mobility.reservations.repository.model.StreetReservationBase;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;

/**
 * Reusable JPA specifications for {@link StreetReservationBase} listings, generic over the concrete
 * subclass so it can be shared by both topology libraries instead of being duplicated per-topology. The
 * license-plate predicate navigates through the (topology-specific) {@code car} association using the
 * untyped, string-based {@code Path} API, so it needs no compile-time dependency on a concrete {@code Car}
 * type.
 */
public final class StreetReservationSpecs {

    private StreetReservationSpecs() {}

    public static <T extends StreetReservationBase> Specification<T> licensePlateEquals(String plate) {
        if (plate == null || plate.isBlank()) return null;
        String normalized = plate.trim().toUpperCase();
        return (root, q, cb) -> cb.equal(cb.upper(root.get("car").get("licensePlate")), normalized);
    }

    public static <T extends StreetReservationBase> Specification<T> userSubEquals(String sub) {
        if (sub == null || sub.isBlank()) return null;
        return (root, q, cb) -> cb.equal(root.get("userSub"), sub);
    }

    public static <T extends StreetReservationBase> Specification<T> createdBetween(OffsetDateTime from, OffsetDateTime to) {
        if (from == null && to == null) return null;
        return (root, q, cb) -> {
            Predicate p = cb.conjunction();
            if (from != null) p = cb.and(p, cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            if (to != null)   p = cb.and(p, cb.lessThanOrEqualTo(root.get("createdAt"), to));
            return p;
        };
    }

    public static <T extends StreetReservationBase> Specification<T> activeAt(OffsetDateTime moment, Boolean active) {
        if (active == null) return null;
        return (root, q, cb) -> active
                ? cb.greaterThan(root.get("expiresAt"), moment)
                : cb.lessThanOrEqualTo(root.get("expiresAt"), moment);
    }
}
