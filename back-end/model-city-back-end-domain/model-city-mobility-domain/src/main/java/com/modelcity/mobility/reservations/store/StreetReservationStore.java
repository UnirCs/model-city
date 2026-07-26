package com.modelcity.mobility.reservations.store;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.mobility.reservations.store.model.StreetReservationView;
import com.modelcity.mobility.reservations.repository.model.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Persistence port for street reservations.
 *
 * <p>Generic over the read type {@code T extends StreetReservationView} (like the use cases / controllers), so
 * a city can bind its own richer view without casting; the write path takes primitives, so there is no write
 * type parameter. The platform default binds {@code T} to the concrete {@code StreetReservation} entity;
 * consumers inject {@code StreetReservationStore<? extends StreetReservationView>}.
 */
@ModelCityExtensionPoint
public interface StreetReservationStore<T extends StreetReservationView> {

    Optional<T> findById(Long id);

    /** Admin listing: license plate filter plus either the active flag (precedence) or a created-at window. */
    Page<T> search(
            String licensePlate, OffsetDateTime from, OffsetDateTime to, Boolean active, Pageable pageable);

    /** Citizen listing: reservations created on/after {@code from}, newest first. */
    Page<T> findUserHistory(String userSub, OffsetDateTime from, Pageable pageable);

    /** Builds and persists a new reservation (status PENDING) for the given car. */
    T create(String userSub, Long carId, Double latitude, Double longitude,
             OffsetDateTime createdAt, OffsetDateTime expiresAt, Long renewedFromId,
             String checkoutSessionId, BigDecimal price);

    /** Updates the status of the reservation matching the given Stripe checkout session id, if any. Returns the updated view. */
    Optional<T> markStatusByCheckoutSession(String checkoutSessionId, ReservationStatus newStatus);
}
