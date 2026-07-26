package com.modelcity.leisure.publicspaces.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;import com.modelcity.leisure.publicspaces.model.ReservableResourceView;
import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceRequestDto;
import com.modelcity.leisure.publicspaces.model.PublicSpaceView;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceRequestDto;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceDto;
import com.modelcity.leisure.publicspaces.store.PublicSpaceStore;
import com.modelcity.leisure.publicspaces.store.ReservableResourceStore;
import lombok.RequiredArgsConstructor;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link GetReservableResourcesUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetReservableResourcesUseCase implements GetReservableResourcesUseCase<ReservableResourceDto> {

    private final PublicSpaceStore<? extends PublicSpaceView, PublicSpaceRequestDto> publicSpaceStore;
    private final ReservableResourceStore<? extends ReservableResourceView, ReservableResourceRequestDto> reservableResourceStore;

    @Override
    @Cacheable(cacheNames = CacheNames.RESERVABLE_RESOURCES,
            key = "#locale + '-' + #publicSpaceId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<ReservableResourceDto> execute(Long publicSpaceId, Pageable pageable, String locale) {
        if (publicSpaceStore.findActiveById(publicSpaceId).isEmpty()) {
            throw new ResourceNotFoundException("PublicSpace", publicSpaceId);
        }
        return reservableResourceStore.findActiveByPublicSpace(publicSpaceId, pageable)
                .map(r -> ReservableResourceDto.from(r, locale));
    }
}
