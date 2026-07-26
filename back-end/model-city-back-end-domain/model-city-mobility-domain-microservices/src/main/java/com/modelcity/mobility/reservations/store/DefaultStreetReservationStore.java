package com.modelcity.mobility.reservations.store;

import com.modelcity.mobility.reservations.repository.StreetReservationRepository;
import com.modelcity.mobility.reservations.repository.StreetReservationSpecs;
import com.modelcity.mobility.reservations.repository.model.ReservationStatus;
import com.modelcity.mobility.reservations.repository.model.StreetReservation;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * JPA adapter for the street reservation persistence port; the default {@link StreetReservationStore} bean.
 *
 * <p>Doubles as an <strong>extension base</strong>: a local deployment may either implement
 * {@link StreetReservationStore} from scratch, or {@code extends} this default and {@code @Override} only the
 * operations it needs, calling {@code super} for the rest. The {@code protected} repository is reachable so a
 * subclass can add its own queries. Either way the platform default backs off, since a subclass is still a
 * {@code StreetReservationStore} bean.
 */
@Slf4j
@Component
@ModelCityDisabledIfInherited
@RequiredArgsConstructor
public class DefaultStreetReservationStore implements StreetReservationStore<StreetReservation> {

    protected final StreetReservationRepository<StreetReservation> streetReservationRepository;

    @PersistenceContext
    protected EntityManager entityManager;

    @Override
    public Optional<StreetReservation> findById(Long id) {
        return streetReservationRepository.findById(id);
    }

    @Override
    public Page<StreetReservation> search(
            String licensePlate, OffsetDateTime from, OffsetDateTime to, Boolean active, Pageable pageable) {
        Specification<StreetReservation> dateOrActive = (active != null)
                ? StreetReservationSpecs.<StreetReservation>activeAt(OffsetDateTime.now(), active)
                : StreetReservationSpecs.<StreetReservation>createdBetween(from, to);
        Specification<StreetReservation> spec = and(
                StreetReservationSpecs.licensePlateEquals(licensePlate), dateOrActive);
        return streetReservationRepository.findAll(spec, pageable);
    }

    @Override
    public Page<StreetReservation> findUserHistory(String userSub, OffsetDateTime from, Pageable pageable) {
        return streetReservationRepository
                .findByUserSubAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(userSub, from, pageable);
    }

    @Override
    public StreetReservation create(String userSub, Long carId, Double latitude, Double longitude,
                                        OffsetDateTime createdAt, OffsetDateTime expiresAt, Long renewedFromId,
                                        String checkoutSessionId, BigDecimal price) {
        StreetReservation reservation = StreetReservation.builder()
                .userSub(userSub)
                .carId(carId)
                .latitude(latitude)
                .longitude(longitude)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .renewedFromId(renewedFromId)
                .stripeCheckoutSessionId(checkoutSessionId)
                .pricePaid(price)
                .status(ReservationStatus.PENDING)
                .build();
        StreetReservation saved = streetReservationRepository.save(reservation);
        // The insertable/updatable=false shadow @ManyToOne navigation (car) is only hydrated by
        // Hibernate on load, not right after an INSERT — refresh so the returned view exposes it.
        entityManager.refresh(saved);
        return saved;
    }

    @Override
    public Optional<StreetReservation> markStatusByCheckoutSession(String checkoutSessionId, ReservationStatus newStatus) {
        Optional<StreetReservation> found = streetReservationRepository.findByStripeCheckoutSessionId(checkoutSessionId);
        found.ifPresentOrElse(reservation -> {
            reservation.setStatus(newStatus);
            streetReservationRepository.save(reservation);
            log.info("Reservation id={} updated to status={} (cs={})", reservation.getId(), newStatus, checkoutSessionId);
        }, () -> log.warn("No reservation found for checkout session id={}", checkoutSessionId));
        return found;
    }

    private static Specification<StreetReservation> and(
            Specification<StreetReservation> a, Specification<StreetReservation> b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.and(b);
    }
}
