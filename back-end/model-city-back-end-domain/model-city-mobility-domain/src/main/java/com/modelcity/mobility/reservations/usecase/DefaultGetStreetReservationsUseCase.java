package com.modelcity.mobility.reservations.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;

import com.modelcity.mobility.reservations.controller.model.StreetReservationDto;
import com.modelcity.mobility.reservations.store.StreetReservationStore;
import com.modelcity.mobility.reservations.store.model.StreetReservationView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Default {@link GetStreetReservationsUseCase} implementation. Component-scanned platform default; disabled at startup when a
 * local deployment provides its own bean for the seam.
 *
 * <p>Doubles as an <strong>extension base</strong>: a local deployment may either implement
 * {@link GetStreetReservationsUseCase} from scratch, or {@code extends} this default and {@code @Override} it,
 * calling {@code super.execute(...)} to reuse the platform behaviour. The {@code protected} store is reachable
 * by a subclass. Either way the default backs off, since a subclass is still a
 * {@code GetStreetReservationsUseCase} bean.
 */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetStreetReservationsUseCase implements GetStreetReservationsUseCase<StreetReservationDto> {

    private static final int PAGE_SIZE = 20;

    protected final StreetReservationStore<? extends StreetReservationView> streetReservationStore;

    @Override
    @Transactional(readOnly = true)
    public Page<StreetReservationDto> execute(
            String licensePlate,
            OffsetDateTime from,
            OffsetDateTime to,
            Boolean active,
            int page) {
        PageRequest pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("createdAt").descending());
        return streetReservationStore.search(licensePlate, from, to, active, pageable)
                .map(StreetReservationDto::from);
    }
}
