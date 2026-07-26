package com.modelcity.core.users.repository;

import com.modelcity.core.users.repository.model.Neighbourhood;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * JPA repository for the {@link Neighbourhood} reference entity.
 */
public interface NeighbourhoodRepository extends JpaRepository<Neighbourhood, Long> {

    /** Finds a neighbourhood by its unique internal name (e.g. {@code "el-recreo-norte"}). */
    Optional<Neighbourhood> findByName(String name);
}

