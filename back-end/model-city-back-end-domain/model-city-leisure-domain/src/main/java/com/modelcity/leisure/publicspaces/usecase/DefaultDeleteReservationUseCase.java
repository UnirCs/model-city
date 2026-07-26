package com.modelcity.leisure.publicspaces.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;
import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.publicspaces.model.SpaceReservationView;
import com.modelcity.leisure.publicspaces.store.SpaceReservationStore;
import com.modelcity.leisure.trails.SystemTrailGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link DeleteReservationUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultDeleteReservationUseCase implements DeleteReservationUseCase {

    private final SpaceReservationStore<? extends SpaceReservationView> spaceReservationStore;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    public void execute(Long publicSpaceId, Long resourceId, Long reservationId, String sub) {
        SpaceReservationView reservation = spaceReservationStore.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", reservationId));
        if (!reservation.getResourceId().equals(resourceId)) {
            throw new ResourceNotFoundException("Reservation", reservationId);
        }
        spaceReservationStore.delete(reservationId);
        systemEventGenerator.spaceReservationDeleted(sub, reservation);
        log.info("Reservation id={} hard-deleted by sub={}", reservationId, sub);
    }
}
