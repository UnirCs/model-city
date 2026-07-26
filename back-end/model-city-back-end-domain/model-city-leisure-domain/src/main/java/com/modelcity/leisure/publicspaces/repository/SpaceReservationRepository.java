package com.modelcity.leisure.publicspaces.repository;

import com.modelcity.leisure.publicspaces.repository.model.SpaceReservationBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.time.LocalDate;
import java.util.List;

/**
 * Generic over the concrete {@link SpaceReservationBase} subclass so both topology libraries — and any city
 * that declares its own entity extending {@code SpaceReservationBase} — reuse this contract instead of
 * forking it. Marked {@code @NoRepositoryBean}: Spring Data cannot proxy an unbound generic repository, so
 * each topology exposes the platform default through its own {@code DefaultSpaceReservationRepository}.
 */
@NoRepositoryBean
public interface SpaceReservationRepository<T extends SpaceReservationBase> extends JpaRepository<T, Long> {

    List<T> findByResourceIdAndReservationDateOrderByStartTimeAsc(Long resourceId, LocalDate date);

    Page<T> findByResourceIdAndReservationDateOrderByStartTimeAsc(Long resourceId, LocalDate date, Pageable pageable);
}
