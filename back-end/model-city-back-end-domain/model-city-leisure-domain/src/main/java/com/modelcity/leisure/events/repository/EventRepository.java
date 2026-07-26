package com.modelcity.leisure.events.repository;

import com.modelcity.leisure.events.repository.model.EventBase;
import com.modelcity.leisure.events.repository.model.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Generic over the concrete {@link EventBase} subclass so a city that declares its own entity (extending
 * {@code EventBase} with extra columns) can reuse this contract instead of forking it. Marked
 * {@code @NoRepositoryBean}: Spring Data cannot proxy an unbound generic repository, so the platform default
 * is exposed through {@link DefaultEventRepository}. A city binds its own subtype the same way:
 * {@code interface MyEventRepository extends EventRepository<MyEvent> {}}.
 */
@NoRepositoryBean
public interface EventRepository<T extends EventBase> extends JpaRepository<T, Long> {

    Optional<T> findByIdAndActiveTrue(Long id);

    /** Filters active events by optional type and optional paid flag, excluding past events. */
    @Query("""
            SELECT e FROM #{#entityName} e
            WHERE e.active = true
              AND e.startsAt >= :now
              AND (:type IS NULL OR e.eventType = :type)
              AND (:paid IS NULL OR e.paid = :paid)
            """)
    Page<T> search(@Param("type") EventType type,
                   @Param("paid") Boolean paid,
                   @Param("now") LocalDateTime now,
                   Pageable pageable);
}
