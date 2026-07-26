package com.modelcity.engagement.alerts.store;

import com.modelcity.engagement.alerts.controller.model.SecurityAlertRequestDto;
import com.modelcity.engagement.alerts.store.model.SecurityAlertView;
import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Persistence port for security alerts.
 *
 * <p>Generic over the read type {@code T extends SecurityAlertView} and the write type
 * {@code R extends SecurityAlertRequestDto} (like the use cases / controllers), so a city can bind its own
 * richer view/request without casting. The platform default binds {@code T} to the concrete
 * {@code SecurityAlert} entity (the microservice stores soft references zoneId/neighbourhoodId, the
 * monolith resolves and links the real Zone/Neighbourhood entities); consumers inject
 * {@code SecurityAlertStore<? extends SecurityAlertView, SecurityAlertRequestDto>}.
 */
@ModelCityExtensionPoint
public interface SecurityAlertStore<T extends SecurityAlertView, R extends SecurityAlertRequestDto> {

    /** Returns paginated alerts whose expiry is after {@code now}, ordered by severity then creation date. */
    Page<T> findActive(OffsetDateTime now, Pageable pageable);

    /** Persists a new alert built from the request (createdAt set to now). */
    T create(R request);

    Optional<T> findById(Long id);

    boolean existsById(Long id);

    void deleteById(Long id);
}
