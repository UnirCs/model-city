package com.modelcity.mobility.sanctions.repository.model;

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
 * Monolith flavour of the sanction: adds a real read-only {@code @ManyToOne} navigation to {@link User}
 * (the issuing operator/admin) on top of the invariant mapping in {@link SanctionBase}.
 */
@Entity
@Table(name = "sanctions")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Sanction extends SanctionBase {

    /** Read-only navigation to the operator/admin that issued the sanction. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_sub", insertable = false, updatable = false)
    private User agent;
}
