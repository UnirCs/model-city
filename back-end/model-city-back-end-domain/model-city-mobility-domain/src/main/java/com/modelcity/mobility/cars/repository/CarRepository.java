package com.modelcity.mobility.cars.repository;

import com.modelcity.mobility.cars.repository.model.CarBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/**
 * Generic over the concrete {@link CarBase} subclass so both topology libraries
 * ({@code model-city-mobility-domain-microservices} / {@code -monolith}) — and any city that declares its
 * own entity extending {@code CarBase} — reuse this contract instead of forking it. Marked
 * {@code @NoRepositoryBean}: Spring Data cannot proxy an unbound generic repository, so each topology
 * exposes the platform default through its own {@code DefaultCarRepository}, binding {@code T} to its
 * local {@code Car} entity.
 */
@NoRepositoryBean
public interface CarRepository<T extends CarBase> extends JpaRepository<T, Long> {

    List<T> findByOwnerSubOrderByCreatedAtDesc(String ownerSub);

    Page<T> findByOwnerSubOrderByCreatedAtDesc(String ownerSub, Pageable pageable);

    Optional<T> findByLicensePlateIgnoreCase(String licensePlate);

    boolean existsByLicensePlateIgnoreCase(String licensePlate);
}
