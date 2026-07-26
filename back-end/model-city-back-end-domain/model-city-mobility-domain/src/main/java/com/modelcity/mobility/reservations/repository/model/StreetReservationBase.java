package com.modelcity.mobility.reservations.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Topology-invariant mapping for a street reservation. Every scalar column is identical in the monolith and
 * the microservices, including the writable {@code car_id} foreign key (kept here as a soft {@code Long}
 * reference, {@link #carId}). The concrete {@code @Entity StreetReservation} in each per-topology domain
 * library adds the real read-only {@code @ManyToOne} navigation to its own (equally topology-divergent)
 * {@code Car} entity — mirroring the {@code insertable = false, updatable = false} shadow-navigation
 * convention already used for {@code User}/{@code Zone}/{@code Neighbourhood} elsewhere in mobility — plus,
 * in the monolith, the shadow navigation to {@code User} (the reserving citizen).
 *
 * <p>This class intentionally does <em>not</em> implement {@code StreetReservationView} — it cannot provide
 * {@code getCar()} without depending on a concrete {@code Car} type, which would defeat the point of the
 * split. The concrete subclass in each topology library implements the view and supplies {@code getCar()}
 * via its own field.
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class StreetReservationBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_sub", nullable = false)
    private String userSub;

    /** Soft (writable) foreign key to the reserved car; the concrete entity adds the read-only navigation. */
    @Column(name = "car_id", nullable = false)
    private Long carId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    /** Optional original reservation when this entry is a renewal. */
    @Column(name = "renewed_from_id")
    private Long renewedFromId;

    /** Stripe Checkout Session ID associated with this reservation. */
    @Column(name = "stripe_checkout_session_id", length = 255)
    private String stripeCheckoutSessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    @Column(name = "price_paid", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal pricePaid = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "EUR";
}
