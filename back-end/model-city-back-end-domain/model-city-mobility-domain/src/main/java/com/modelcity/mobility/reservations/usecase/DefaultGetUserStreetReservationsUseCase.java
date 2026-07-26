package com.modelcity.mobility.reservations.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;

import com.modelcity.mobility.reservations.controller.model.StreetReservationDto;
import com.modelcity.mobility.reservations.store.StreetReservationStore;
import com.modelcity.mobility.reservations.store.model.StreetReservationView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

/**
 * Default {@link GetUserStreetReservationsUseCase} implementation. Component-scanned platform default; disabled at startup when a
 * local deployment provides its own bean for the seam.
 *
 * <p>Doubles as an <strong>extension base</strong>: a local deployment may either implement
 * {@link GetUserStreetReservationsUseCase} from scratch, or {@code extends} this default and {@code @Override}
 * it, calling {@code super.execute(...)} to reuse the platform behaviour. The {@code protected} store is
 * reachable by a subclass. Either way the default backs off, since a subclass is still a
 * {@code GetUserStreetReservationsUseCase} bean.
 */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetUserStreetReservationsUseCase implements GetUserStreetReservationsUseCase<StreetReservationDto> {

    private static final int HISTORY_DAYS = 30;

    protected final StreetReservationStore<? extends StreetReservationView> streetReservationStore;

    @Override
    @Transactional(readOnly = true)
    public Page<StreetReservationDto> execute(String userId, String sub, Pageable pageable) {
        if (!userId.equals(sub)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Caller does not match path user id");
        }
        OffsetDateTime from = OffsetDateTime.now().minusDays(HISTORY_DAYS);
        return streetReservationStore.findUserHistory(userId, from, pageable)
                .map(StreetReservationDto::from);
    }
}
