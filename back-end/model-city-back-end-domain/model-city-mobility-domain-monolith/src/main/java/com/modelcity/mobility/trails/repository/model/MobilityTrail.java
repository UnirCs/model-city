package com.modelcity.mobility.trails.repository.model;

import com.modelcity.core.users.repository.model.Neighbourhood;
import com.modelcity.core.users.repository.model.User;
import com.modelcity.core.users.repository.model.Zone;
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
 * Monolith flavour of the mobility audit-log entry: adds real read-only {@code @ManyToOne} navigations to
 * {@link User}, {@link Neighbourhood} and {@link Zone} on top of the invariant mapping in
 * {@link MobilityTrailBase}.
 */
@Entity
@Table(name = "mobility_trails")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class MobilityTrail extends MobilityTrailBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_user_id", insertable = false, updatable = false)
    private User responsibleUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "neighbourhood_id", insertable = false, updatable = false)
    private Neighbourhood neighbourhood;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", insertable = false, updatable = false)
    private Zone zone;
}
