package com.modelcity.mobility.reservations.repository.model;

import com.modelcity.core.users.repository.model.User;
import com.modelcity.mobility.cars.repository.model.Car;
import com.modelcity.mobility.reservations.store.model.StreetReservationView;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Monolith flavour of the street reservation: adds real read-only {@code @ManyToOne} navigations to
 * {@link Car} (over the shared writable {@code car_id} column, since {@code Car} is itself topology-specific
 * and cannot be referenced from the shared base) and to {@link User} (the reserving citizen), on top of the
 * invariant mapping in {@link StreetReservationBase}.
 */
@Entity
@Table(name = "street_reservations")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class StreetReservation extends StreetReservationBase implements StreetReservationView {

    /** Read-only navigation to the reserved car. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", insertable = false, updatable = false)
    private Car car;

    /** Read-only navigation to the reserving user. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_sub", insertable = false, updatable = false)
    private User user;
}
