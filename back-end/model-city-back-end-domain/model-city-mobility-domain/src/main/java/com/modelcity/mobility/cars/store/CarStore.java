package com.modelcity.mobility.cars.store;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.mobility.cars.controller.model.CarRequestDto;
import com.modelcity.mobility.cars.store.model.CarView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port for cars.
 *
 * <p>Generic over the read type {@code T extends CarView} and the write type {@code R extends CarRequestDto}
 * (like the use cases / controllers), so a city can bind its own richer view/request without casting. The
 * platform default binds {@code T} to the concrete {@code Car} entity; consumers inject
 * {@code CarStore<? extends CarView, CarRequestDto>}.
 */
@ModelCityExtensionPoint
public interface CarStore<T extends CarView, R extends CarRequestDto> {

    boolean existsByLicensePlate(String licensePlate);

    /** Builds and persists a new car for the owner with the (already normalized) license plate. */
    T create(String ownerSub, String licensePlate, R request);

    Optional<T> findById(Long id);

    Page<T> findByOwner(String ownerSub, Pageable pageable);

    /** All cars of the owner, newest first (used to resolve the owner's plates). */
    List<T> findByOwner(String ownerSub);
}
