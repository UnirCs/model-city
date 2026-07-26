package com.modelcity.mobility.reservations.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;

import com.modelcity.mobility.cars.controller.model.CarRequestDto;
import com.modelcity.mobility.cars.store.model.CarView;
import com.modelcity.mobility.cars.store.CarStore;
import com.modelcity.mobility.reservations.controller.model.StreetReservationDto;
import com.modelcity.mobility.reservations.controller.model.StreetReservationRequestDto;
import com.modelcity.mobility.reservations.store.model.StreetReservationView;
import com.modelcity.mobility.reservations.store.StreetReservationStore;
import com.modelcity.mobility.trails.SystemTrailGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

/**
 * Default {@link RenewStreetReservationUseCase} implementation. Component-scanned platform default; disabled at startup when a
 * local deployment provides its own bean for the seam.
 *
 * <p>Doubles as an <strong>extension base</strong>: a local deployment may either implement
 * {@link RenewStreetReservationUseCase} from scratch, or {@code extends} this default and {@code @Override}
 * it, calling {@code super.execute(...)} to reuse the platform behaviour. Collaborators are {@code protected}
 * so a subclass can reach them. Either way the default backs off, since a subclass is still a
 * {@code RenewStreetReservationUseCase} bean.
 */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultRenewStreetReservationUseCase implements RenewStreetReservationUseCase<StreetReservationDto, StreetReservationRequestDto> {

    protected final StreetReservationStore<? extends StreetReservationView> streetReservationStore;
    protected final CarStore<? extends CarView, CarRequestDto> carStore;
    protected final SystemTrailGenerator systemEventGenerator;

    @Override
    public StreetReservationDto execute(String userId, String sub, Long reservationId,
                                        StreetReservationRequestDto request) {
        if (!userId.equals(sub)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Caller does not match path user id");
        }
        StreetReservationView original = streetReservationStore.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));
        if (!original.getUserSub().equals(sub)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Reservation belongs to a different user");
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (!original.getExpiresAt().isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "Original reservation is no longer active");
        }
        CarView car = carStore.findById(request.getCarId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
        if (!car.getOwnerSub().equals(sub)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Car does not belong to the caller");
        }

        StreetReservationView saved = streetReservationStore.create(
                sub, request.getCarId(), request.getLatitude(), request.getLongitude(),
                now, now.plusMinutes(request.getDurationMinutes()), original.getId(),
                request.getCheckoutSessionId(), request.getPrice());
        systemEventGenerator.streetReservationRenewed(saved);
        log.info("StreetReservation renewal id={} from original={} sub={} session={}",
                saved.getId(), original.getId(), sub, request.getCheckoutSessionId());
        return StreetReservationDto.from(saved);
    }
}
