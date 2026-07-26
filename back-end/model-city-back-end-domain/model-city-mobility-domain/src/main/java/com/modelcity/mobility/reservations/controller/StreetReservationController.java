package com.modelcity.mobility.reservations.controller;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.common.security.ModelCityAccess;
import com.modelcity.mobility.reservations.controller.model.StreetReservationDto;
import com.modelcity.mobility.reservations.usecase.GetStreetReservationsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

/**
 * Admin/operator endpoint to inspect street reservations with multiple filters.
 *
 * <p>Overridable base controller (abstract): the platform registers {@link DefaultStreetReservationController}
 * as the default bean. A local deployment overrides by declaring its own {@code @RestController} that
 * {@code extends StreetReservationController}; the default then backs off.
 */
@Slf4j
@RestController
@RequestMapping("/street-reservations")
@RequiredArgsConstructor
@ModelCityExtensionPoint
public abstract class StreetReservationController<T extends StreetReservationDto> {

    protected final GetStreetReservationsUseCase<T> getStreetReservationsUseCase;

    /**
     * Lists reservations. Filters: licensePlate, from/to (created_at window) and active (takes precedence
     * over from/to). Admin or mobility agent only.
     */
    @GetMapping
    @ModelCityAccess.PlatformAdmin
    @ModelCityAccess.MobilityAgent
    public Page<T> getStreetReservations(
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestParam(required = false) String licensePlate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page) {
        log.debug("GET /street-reservations sub={} plate={} from={} to={} active={} page={}",
                sub, licensePlate, from, to, active, page);
        return getStreetReservationsUseCase.execute(licensePlate, from, to, active, page);
    }
}
