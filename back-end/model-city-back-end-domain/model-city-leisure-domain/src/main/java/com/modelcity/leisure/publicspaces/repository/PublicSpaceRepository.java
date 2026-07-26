package com.modelcity.leisure.publicspaces.repository;

import com.modelcity.leisure.publicspaces.repository.model.PublicSpaceBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

/**
 * Generic over the concrete {@link PublicSpaceBase} subclass so a city that declares its own entity
 * (extending {@code PublicSpaceBase} with extra columns) can reuse this contract instead of forking it.
 * Marked {@code @NoRepositoryBean}: Spring Data cannot proxy an unbound generic repository, so the platform
 * default is exposed through {@link DefaultPublicSpaceRepository}. A city binds its own subtype the same way:
 * {@code interface MyPublicSpaceRepository extends PublicSpaceRepository<MyPublicSpace> {}}.
 */
@NoRepositoryBean
public interface PublicSpaceRepository<T extends PublicSpaceBase> extends JpaRepository<T, Long> {

    Page<T> findByActiveTrue(Pageable pageable);

    Optional<T> findByIdAndActiveTrue(Long id);
}

