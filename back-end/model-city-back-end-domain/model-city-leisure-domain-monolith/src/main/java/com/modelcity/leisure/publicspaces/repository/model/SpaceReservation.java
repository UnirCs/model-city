package com.modelcity.leisure.publicspaces.repository.model;

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

/** Monolith flavour: adds a real read-only {@code @ManyToOne} navigation to {@link User} (booking citizen). */
@Entity
@Table(name = "space_reservations")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class SpaceReservation extends SpaceReservationBase {

    /** Read-only navigation to the booking citizen. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_sub", insertable = false, updatable = false)
    private User citizen;
}
