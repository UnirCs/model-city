package com.modelcity.leisure.publicspaces.repository;

import com.modelcity.leisure.publicspaces.repository.model.ReservableResourceBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/**
 * Generic over the concrete {@link ReservableResourceBase} subclass so a city that declares its own entity
 * (extending {@code ReservableResourceBase} with extra columns) can reuse this contract instead of forking
 * it. Marked {@code @NoRepositoryBean}: Spring Data cannot proxy an unbound generic repository, so the
 * platform default is exposed through {@link DefaultReservableResourceRepository}. A city binds its own
 * subtype the same way: {@code interface MyReservableResourceRepository extends
 * ReservableResourceRepository<MyReservableResource> {}}.
 */
@NoRepositoryBean
public interface ReservableResourceRepository<T extends ReservableResourceBase> extends JpaRepository<T, Long> {

    List<T> findByPublicSpaceIdAndActiveTrueOrderByIdAsc(Long publicSpaceId);

    Page<T> findByPublicSpaceIdAndActiveTrueOrderByIdAsc(Long publicSpaceId, Pageable pageable);

    Optional<T> findByIdAndActiveTrue(Long id);

    Optional<T> findByIdAndPublicSpaceIdAndActiveTrue(Long id, Long publicSpaceId);
}

