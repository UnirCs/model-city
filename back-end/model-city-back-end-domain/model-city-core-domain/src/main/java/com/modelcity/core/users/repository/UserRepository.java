package com.modelcity.core.users.repository;

import com.modelcity.core.users.repository.model.User;
import com.modelcity.core.users.repository.model.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * JPA repository for the {@link User} entity.
 * PK is the Auth0 {@code sub} claim, so {@code findById} and {@code existsById} work out of the box.
 */
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    List<User> findAllByRole(UserRole role);

    List<User> findAllByRoleNot(UserRole role);

    Page<User> findAllByRole(UserRole role, Pageable pageable);

    Page<User> findAllByRoleNot(UserRole role, Pageable pageable);
}
