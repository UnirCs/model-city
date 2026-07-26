package com.modelcity.mobility.reservations.repository;

import com.modelcity.mobility.reservations.repository.model.StreetReservationBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Generic over the concrete {@link StreetReservationBase} subclass so both topology libraries — and any
 * city that declares its own entity extending {@code StreetReservationBase} — reuse this contract instead
 * of forking it. Marked {@code @NoRepositoryBean}: each topology exposes the platform default through its
 * own {@code DefaultStreetReservationRepository}, binding {@code T} to its local {@code StreetReservation}
 * entity.
 */
@NoRepositoryBean
public interface StreetReservationRepository<T extends StreetReservationBase>
        extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

    /** Returns the user reservations that are active or were created in the given window, newest first. */
    List<T> findByUserSubAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(String userSub, OffsetDateTime from);

    Page<T> findByUserSubAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            String userSub, OffsetDateTime from, Pageable pageable);

    /** Finds a reservation by its Stripe Checkout Session ID. */
    Optional<T> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);
}
