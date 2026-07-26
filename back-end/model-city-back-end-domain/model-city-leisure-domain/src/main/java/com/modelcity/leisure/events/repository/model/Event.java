package com.modelcity.leisure.events.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Cultural / leisure event held at a city place. Platform default entity: all columns live in
 * {@link EventBase}. A city that needs extra columns declares its own {@code @Entity} extending
 * {@link EventBase} instead of editing this class.
 */
@Entity
@Table(name = "events")
@SuperBuilder
@NoArgsConstructor
public class Event extends EventBase {
}
