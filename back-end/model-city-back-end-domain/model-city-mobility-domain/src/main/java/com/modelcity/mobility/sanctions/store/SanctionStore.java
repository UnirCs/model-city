package com.modelcity.mobility.sanctions.store;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.mobility.sanctions.controller.model.SanctionRequestDto;
import com.modelcity.mobility.sanctions.store.model.SanctionView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;

/**
 * Persistence port for sanctions.
 *
 * <p>Generic over the read type {@code T extends SanctionView} and the write type
 * {@code R extends SanctionRequestDto} (like the use cases / controllers), so a city can bind its own richer
 * view/request without casting. The platform default binds {@code T} to the concrete {@code Sanction} entity;
 * consumers inject {@code SanctionStore<? extends SanctionView, SanctionRequestDto>}.
 */
@ModelCityExtensionPoint
public interface SanctionStore<T extends SanctionView, R extends SanctionRequestDto> {

    /** Builds and persists a new sanction (license plate normalized to upper-case). */
    T create(String agentSub, R request);

    Optional<T> findById(Long id);

    /** Admin listing: optional license-plate and created-at window filters. */
    Page<T> search(String licensePlate, OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    /** Citizen listing: sanctions whose plate is in the given set, newest first. */
    Page<T> findByPlatesIn(Collection<String> plates, Pageable pageable);
}
