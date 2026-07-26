package com.modelcity.mobility.cars.repository.model;

import com.modelcity.core.users.repository.model.User;
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
 * Monolith flavour of the car: because the monolith's single persistence unit owns the {@code users}
 * table, it adds a real read-only {@code @ManyToOne} navigation to {@link User} on top of the invariant
 * mapping in {@link CarBase}. This relationship cannot exist in the microservices flavour.
 */
@Entity
@Table(name = "cars")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Car extends CarBase {

    /** Read-only navigation to the owning user. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_sub", insertable = false, updatable = false)
    private User owner;
}
