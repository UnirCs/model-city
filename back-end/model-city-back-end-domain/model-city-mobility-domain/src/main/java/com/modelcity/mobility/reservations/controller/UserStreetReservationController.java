package com.modelcity.mobility.reservations.controller;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.mobility.reservations.controller.model.StreetReservationDto;
import com.modelcity.mobility.reservations.controller.model.StreetReservationRequestDto;
import com.modelcity.mobility.reservations.usecase.CreateStreetReservationUseCase;
import com.modelcity.mobility.reservations.usecase.GetUserStreetReservationsUseCase;
import com.modelcity.mobility.reservations.usecase.RenewStreetReservationUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Citizen street reservation endpoints.
 *
 * <p>Overridable base controller (abstract): the platform registers
 * {@link DefaultUserStreetReservationController} as the default bean. A local deployment overrides by
 * declaring its own {@code @RestController} that {@code extends UserStreetReservationController}; the default
 * then backs off.
 */
@Slf4j
@RestController
@RequestMapping("/users/{userId}/street-reservations")
@RequiredArgsConstructor
@ModelCityExtensionPoint
public abstract class UserStreetReservationController<T extends StreetReservationDto, R extends StreetReservationRequestDto> {

    protected final CreateStreetReservationUseCase<T, R> createStreetReservationUseCase;
    protected final GetUserStreetReservationsUseCase<T> getUserStreetReservationsUseCase;
    protected final RenewStreetReservationUseCase<T, R> renewStreetReservationUseCase;

    /** Creates a new SER reservation for the caller. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public T createReservation(
            @PathVariable String userId,
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid R request) {
        log.debug("POST /users/{}/street-reservations sub={}", userId, sub);
        return createStreetReservationUseCase.execute(userId, sub, request);
    }

    /** Returns active reservations and history of the last 30 days for the caller. */
    @GetMapping
    public Page<T> getReservations(
            @PathVariable String userId,
            @RequestHeader("X-Auth-Sub") String sub,
            @PageableDefault(size = 10) Pageable pageable) {
        log.debug("GET /users/{}/street-reservations sub={}", userId, sub);
        return getUserStreetReservationsUseCase.execute(userId, sub, pageable);
    }

    /** Renews an active reservation. */
    @PostMapping("/{reservationId}/renewals")
    @ResponseStatus(HttpStatus.CREATED)
    public T renewReservation(
            @PathVariable String userId,
            @PathVariable Long reservationId,
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid R request) {
        log.debug("POST /users/{}/street-reservations/{}/renewals sub={}", userId, reservationId, sub);
        return renewStreetReservationUseCase.execute(userId, sub, reservationId, request);
    }
}
