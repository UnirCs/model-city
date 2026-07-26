package com.modelcity.leisure.publicspaces.controller;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.common.security.ModelCityAccess;
import com.modelcity.leisure.publicspaces.controller.model.ReservationDto;
import com.modelcity.leisure.publicspaces.controller.model.ReservationRequestDto;
import com.modelcity.leisure.publicspaces.usecase.CreateReservationUseCase;
import com.modelcity.leisure.publicspaces.usecase.DeleteReservationUseCase;
import com.modelcity.leisure.publicspaces.usecase.GetReservationsUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Reservation endpoints scoped to a specific reservable resource.
 *
 * <p>Overridable base controller (abstract): the platform registers {@link DefaultReservationController} as
 * the default bean. A local deployment overrides by declaring its own {@code @RestController} that
 * {@code extends ReservationController}; the default then backs off.
 */
@Slf4j
@RestController
@RequestMapping("/public-spaces/{publicSpaceId}/resources/{resourceId}/reservations")
@RequiredArgsConstructor
@ModelCityExtensionPoint
public abstract class ReservationController<
        T extends ReservationDto,
        R extends ReservationRequestDto> {

    protected final GetReservationsUseCase<T> getReservationsUseCase;
    protected final CreateReservationUseCase<T, R> createReservationUseCase;
    protected final DeleteReservationUseCase deleteReservationUseCase;

    /**
     * Lists reservations for a resource on a given date.
     * Admin/operator callers see citizen identification; everyone else sees only the time slot.
     */
    @GetMapping
    public Page<T> getReservations(
            @PathVariable Long publicSpaceId,
            @PathVariable Long resourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader(value = "X-Auth-Sub", required = false) String sub,
            @PageableDefault(size = 50) Pageable pageable) {
        log.debug("GET /public-spaces/{}/resources/{}/reservations date={} sub={}", publicSpaceId, resourceId, date, sub);
        return getReservationsUseCase.execute(publicSpaceId, resourceId, date, sub, pageable);
    }

    /** Creates a reservation as a citizen. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public T createReservation(
            @PathVariable Long publicSpaceId,
            @PathVariable Long resourceId,
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid R request) {
        log.debug("POST /public-spaces/{}/resources/{}/reservations sub={}", publicSpaceId, resourceId, sub);
        return createReservationUseCase.execute(publicSpaceId, resourceId, sub, request);
    }

    /** Hard-deletes a reservation. Operator or admin. */
    @DeleteMapping("/{reservationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ModelCityAccess.PlatformAdmin
    @ModelCityAccess.Operator
    public void deleteReservation(
            @PathVariable Long publicSpaceId,
            @PathVariable Long resourceId,
            @PathVariable Long reservationId,
            @RequestHeader("X-Auth-Sub") String sub) {
        log.debug("DELETE /public-spaces/{}/resources/{}/reservations/{} sub={}",
                publicSpaceId, resourceId, reservationId, sub);
        deleteReservationUseCase.execute(publicSpaceId, resourceId, reservationId, sub);
    }
}
