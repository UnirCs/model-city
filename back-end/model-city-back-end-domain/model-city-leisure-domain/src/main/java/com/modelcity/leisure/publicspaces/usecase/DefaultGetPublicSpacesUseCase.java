package com.modelcity.leisure.publicspaces.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;import com.modelcity.leisure.publicspaces.model.PublicSpaceView;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceRequestDto;

import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceSummaryDto;
import com.modelcity.leisure.publicspaces.store.PublicSpaceStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link GetPublicSpacesUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetPublicSpacesUseCase implements GetPublicSpacesUseCase<PublicSpaceSummaryDto> {

    private static final int PAGE_SIZE = 6;

    private final PublicSpaceStore<? extends PublicSpaceView, PublicSpaceRequestDto> publicSpaceStore;

    @Override
    @Cacheable(cacheNames = CacheNames.PUBLIC_SPACES, key = "#locale + '-' + #page")
    @Transactional(readOnly = true)
    public Page<PublicSpaceSummaryDto> execute(int page, String locale) {
        PageRequest pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
        return publicSpaceStore.findActive(pageable).map(s -> PublicSpaceSummaryDto.from(s, locale));
    }
}
