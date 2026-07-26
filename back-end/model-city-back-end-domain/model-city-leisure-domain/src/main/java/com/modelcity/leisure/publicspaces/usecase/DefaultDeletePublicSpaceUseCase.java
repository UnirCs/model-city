package com.modelcity.leisure.publicspaces.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;import com.modelcity.leisure.publicspaces.model.ReservableResourceView;
import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceRequestDto;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceRequestDto;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.publicspaces.model.PublicSpaceView;
import com.modelcity.leisure.publicspaces.store.PublicSpaceStore;
import com.modelcity.leisure.publicspaces.store.ReservableResourceStore;
import com.modelcity.leisure.trails.SystemTrailGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link DeletePublicSpaceUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultDeletePublicSpaceUseCase implements DeletePublicSpaceUseCase {

    private final PublicSpaceStore<? extends PublicSpaceView, PublicSpaceRequestDto> publicSpaceStore;
    private final ReservableResourceStore<? extends ReservableResourceView, ReservableResourceRequestDto> reservableResourceStore;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.PUBLIC_SPACE, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.PUBLIC_SPACES, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.RESERVABLE_RESOURCES, allEntries = true)
    })
    public void execute(Long id, String sub) {
        PublicSpaceView space = publicSpaceStore.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PublicSpace", id));
        publicSpaceStore.softDelete(id);
        reservableResourceStore.softDeleteByPublicSpace(id);
        systemEventGenerator.publicSpaceDeleted(sub, space);
        log.info("PublicSpace id={} soft-deleted by sub={}", id, sub);
    }
}
