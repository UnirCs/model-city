package com.modelcity.mobility.reservations.repository.model;

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
 * Microservices flavour of the street reservation: the car is exposed via a real, read-only
 * {@code @ManyToOne} shadow navigation over the writable {@code car_id} column inherited from
 * {@link StreetReservationBase} — the same convention already used for {@code User}/{@code Zone} elsewhere
 * in mobility, applied here because {@code Car} is itself topology-divergent and cannot be referenced from
 * the shared base. There is no navigation to {@code User} because the microservice's persistence unit has
 * no such entity.
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
}
