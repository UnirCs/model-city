package com.modelcity.leisure.publicspaces.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;import com.modelcity.leisure.publicspaces.model.ReservableResourceView;
import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceRequestDto;

import com.modelcity.common.client.CoreClient;
import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.publicspaces.controller.model.ReservationDto;
import com.modelcity.leisure.publicspaces.controller.model.ReservationRequestDto;
import com.modelcity.leisure.publicspaces.model.SpaceReservationView;
import com.modelcity.leisure.publicspaces.store.ReservableResourceStore;
import com.modelcity.leisure.publicspaces.store.SpaceReservationStore;
import com.modelcity.leisure.trails.SystemTrailGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalTime;

/** Default {@link CreateReservationUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultCreateReservationUseCase implements CreateReservationUseCase<ReservationDto, ReservationRequestDto> {

    private static final LocalTime WINDOW_START = LocalTime.of(9, 0);
    private static final LocalTime WINDOW_END = LocalTime.of(19, 0);
    private static final Duration MAX_DURATION = Duration.ofHours(2);

    private final ReservableResourceStore<? extends ReservableResourceView, ReservableResourceRequestDto> reservableResourceStore;
    private final SpaceReservationStore<? extends SpaceReservationView> spaceReservationStore;
    private final CoreClient coreClient;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    public ReservationDto execute(Long publicSpaceId, Long resourceId, String citizenSub, ReservationRequestDto request) {
        if (reservableResourceStore.findActiveByIdAndPublicSpace(resourceId, publicSpaceId).isEmpty()) {
            throw new ResourceNotFoundException("ReservableResource", resourceId);
        }
        validateTimeRange(request.getStartTime(), request.getEndTime());
        validateNoOverlap(resourceId, request);

        SpaceReservationView saved = spaceReservationStore.create(
                resourceId, citizenSub, safeFetchName(citizenSub),
                request.getReservationDate(), request.getStartTime(), request.getEndTime());
        systemEventGenerator.spaceReservationCreated(citizenSub, saved);
        log.info("Reservation id={} created on resource={} by sub={}", saved.getId(), resourceId, citizenSub);
        return ReservationDto.privilegedView(saved);
    }

    private void validateTimeRange(LocalTime start, LocalTime end) {
        if (!end.isAfter(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }
        if (start.isBefore(WINDOW_START) || end.isAfter(WINDOW_END)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservations must fall between 09:00 and 19:00");
        }
        if (Duration.between(start, end).compareTo(MAX_DURATION) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation duration must not exceed 2 hours");
        }
    }

    private void validateNoOverlap(Long resourceId, ReservationRequestDto request) {
        boolean overlap = spaceReservationStore
                .findByResourceAndDate(resourceId, request.getReservationDate())
                .stream()
                .anyMatch(existing -> existing.getStartTime().isBefore(request.getEndTime())
                        && existing.getEndTime().isAfter(request.getStartTime()));
        if (overlap) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The requested time slot overlaps with an existing reservation");
        }
    }

    private String safeFetchName(String sub) {
        try {
            return coreClient.getUserName(sub);
        } catch (Exception ex) {
            log.warn("Could not resolve citizen name for sub={}: {}", sub, ex.getMessage());
            return null;
        }
    }
}
