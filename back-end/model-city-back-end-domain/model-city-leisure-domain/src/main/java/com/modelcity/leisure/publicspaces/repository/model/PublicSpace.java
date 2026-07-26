package com.modelcity.leisure.publicspaces.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * A public facility (sport centre, library...) hosting reservable resources. Platform default entity: all
 * columns live in {@link PublicSpaceBase}. A city that needs extra columns declares its own {@code @Entity}
 * extending {@link PublicSpaceBase} instead of editing this class.
 */
@Entity
@Table(name = "public_spaces")
@SuperBuilder
@NoArgsConstructor
public class PublicSpace extends PublicSpaceBase {
}
