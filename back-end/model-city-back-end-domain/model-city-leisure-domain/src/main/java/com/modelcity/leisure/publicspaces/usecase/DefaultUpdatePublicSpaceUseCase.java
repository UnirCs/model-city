package com.modelcity.leisure.publicspaces.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;
import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceDto;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceRequestDto;
import com.modelcity.leisure.publicspaces.model.PublicSpaceView;
import com.modelcity.leisure.publicspaces.store.PublicSpaceStore;
import com.modelcity.leisure.trails.SystemTrailGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link UpdatePublicSpaceUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultUpdatePublicSpaceUseCase implements UpdatePublicSpaceUseCase<PublicSpaceDto, PublicSpaceRequestDto> {

    private final PublicSpaceStore<? extends PublicSpaceView, PublicSpaceRequestDto> publicSpaceStore;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.PUBLIC_SPACE, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.PUBLIC_SPACES, allEntries = true)
    })
    public PublicSpaceDto execute(Long id, String sub, PublicSpaceRequestDto request, String locale) {
        if (publicSpaceStore.findActiveById(id).isEmpty()) {
            throw new ResourceNotFoundException("PublicSpace", id);
        }
        PublicSpaceView saved = publicSpaceStore.update(id, request);
        systemEventGenerator.publicSpaceUpdated(sub, saved);
        log.info("PublicSpace id={} updated by sub={}", id, sub);
        return PublicSpaceDto.from(saved, locale);
    }
}
