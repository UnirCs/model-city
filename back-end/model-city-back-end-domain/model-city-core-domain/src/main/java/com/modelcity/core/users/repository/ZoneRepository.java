package com.modelcity.core.users.repository;

import com.modelcity.core.users.repository.model.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * JPA repository for the {@link Zone} reference entity.
 */
public interface ZoneRepository extends JpaRepository<Zone, Long> {

    /** Finds a zone by its unique internal name (e.g. {@code "north-district"}). */
    Optional<Zone> findByName(String name);
}

