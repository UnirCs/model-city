package com.modelcity.core.users.repository;

import com.modelcity.core.users.repository.model.User;
import com.modelcity.core.users.repository.model.UserRole;
import org.springframework.data.jpa.domain.Specification;

/** Reusable JPA specifications for the admin user listing (citizens / workers). */
public final class UserSpecs {

    private UserSpecs() {}

    public static Specification<User> roleEquals(UserRole role) {
        if (role == null) return null;
        return (root, q, cb) -> cb.equal(root.get("role"), role);
    }

    /** Citizens (when {@code true}) or non-citizens / workers (when {@code false}). Null = no filter. */
    public static Specification<User> citizen(Boolean citizen) {
        if (citizen == null) return null;
        return (root, q, cb) -> citizen
                ? cb.equal(root.get("role"), UserRole.MODEL_CITY_CITIZEN)
                : cb.notEqual(root.get("role"), UserRole.MODEL_CITY_CITIZEN);
    }

    public static Specification<User> nameContains(String name) {
        if (name == null || name.isBlank()) return null;
        String like = "%" + name.trim().toLowerCase() + "%";
        return (root, q, cb) -> cb.like(cb.lower(root.get("name")), like);
    }

    public static Specification<User> neighbourhoodIdEquals(Long neighbourhoodId) {
        if (neighbourhoodId == null) return null;
        return (root, q, cb) -> cb.equal(root.get("neighbourhood").get("id"), neighbourhoodId);
    }

    public static Specification<User> and(Specification<User> a, Specification<User> b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.and(b);
    }
}
