package com.modelcity.leisure.publicspaces.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;import com.modelcity.leisure.publicspaces.model.PublicSpaceView;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceRequestDto;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceDto;
import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceRequestDto;
import com.modelcity.leisure.publicspaces.model.ReservableResourceView;
import com.modelcity.leisure.publicspaces.store.PublicSpaceStore;
import com.modelcity.leisure.publicspaces.store.ReservableResourceStore;
import com.modelcity.leisure.trails.SystemTrailGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link CreateReservableResourceUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultCreateReservableResourceUseCase implements CreateReservableResourceUseCase<ReservableResourceDto, ReservableResourceRequestDto> {

    private final PublicSpaceStore<? extends PublicSpaceView, PublicSpaceRequestDto> publicSpaceStore;
    private final ReservableResourceStore<? extends ReservableResourceView, ReservableResourceRequestDto> reservableResourceStore;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    @CacheEvict(cacheNames = CacheNames.RESERVABLE_RESOURCES, allEntries = true)
    public ReservableResourceDto execute(Long publicSpaceId, String sub, ReservableResourceRequestDto request, String locale) {
        if (publicSpaceStore.findActiveById(publicSpaceId).isEmpty()) {
            throw new ResourceNotFoundException("PublicSpace", publicSpaceId);
        }
        ReservableResourceView saved = reservableResourceStore.create(publicSpaceId, request);
        systemEventGenerator.reservableResourceCreated(sub, saved);
        log.info("ReservableResource id={} created in space={} by sub={}", saved.getId(), publicSpaceId, sub);
        return ReservableResourceDto.from(saved, locale);
    }
}
