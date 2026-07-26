package com.modelcity.leisure.publicspaces.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;import com.modelcity.leisure.publicspaces.model.ReservableResourceView;
import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceRequestDto;
import com.modelcity.leisure.publicspaces.model.SpaceReservationView;

import com.modelcity.common.client.CoreClient;
import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.publicspaces.controller.model.ReservationDto;
import com.modelcity.leisure.publicspaces.store.ReservableResourceStore;
import com.modelcity.leisure.publicspaces.store.SpaceReservationStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

/** Default {@link GetReservationsUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetReservationsUseCase implements GetReservationsUseCase<ReservationDto> {

    private static final Set<String> PRIVILEGED_ROLES = Set.of(
            "MODEL-CITY-PLATFORM-ADMIN", "MODEL-CITY-OPERATOR");

    private final ReservableResourceStore<? extends ReservableResourceView, ReservableResourceRequestDto> reservableResourceStore;
    private final SpaceReservationStore<? extends SpaceReservationView> spaceReservationStore;
    private final CoreClient coreClient;

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationDto> execute(Long publicSpaceId, Long resourceId, LocalDate date, String callerSub, Pageable pageable) {
        if (reservableResourceStore.findActiveByIdAndPublicSpace(resourceId, publicSpaceId).isEmpty()) {
            throw new ResourceNotFoundException("ReservableResource", resourceId);
        }
        boolean privileged = isPrivileged(callerSub);
        return spaceReservationStore
                .findByResourceAndDate(resourceId, date, pageable)
                .map(r -> privileged ? ReservationDto.privilegedView(r) : ReservationDto.publicView(r));
    }

    private boolean isPrivileged(String sub) {
        if (sub == null || sub.isBlank()) return false;
        try {
            String role = coreClient.getUserRole(sub);
            return role != null && PRIVILEGED_ROLES.contains(role);
        } catch (Exception ignored) {
            return false;
        }
    }
}
