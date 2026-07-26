package com.modelcity.leisure.publicspaces.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceRequestDto;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.publicspaces.model.ReservableResourceView;
import com.modelcity.leisure.publicspaces.store.ReservableResourceStore;
import com.modelcity.leisure.trails.SystemTrailGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link DeleteReservableResourceUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultDeleteReservableResourceUseCase implements DeleteReservableResourceUseCase {

    private final ReservableResourceStore<? extends ReservableResourceView, ReservableResourceRequestDto> reservableResourceStore;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    @CacheEvict(cacheNames = CacheNames.RESERVABLE_RESOURCES, allEntries = true)
    public void execute(Long publicSpaceId, Long resourceId, String sub) {
        ReservableResourceView resource = reservableResourceStore.findActiveByIdAndPublicSpace(resourceId, publicSpaceId)
                .orElseThrow(() -> new ResourceNotFoundException("ReservableResource", resourceId));
        reservableResourceStore.softDelete(resourceId, publicSpaceId);
        systemEventGenerator.reservableResourceDeleted(sub, resource);
        log.info("ReservableResource id={} soft-deleted by sub={}", resourceId, sub);
    }
}
