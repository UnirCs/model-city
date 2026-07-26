package com.modelcity.leisure.publicspaces.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;import com.modelcity.leisure.publicspaces.model.PublicSpaceView;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceRequestDto;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceDto;
import com.modelcity.leisure.publicspaces.store.PublicSpaceStore;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link GetPublicSpaceForEditUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetPublicSpaceForEditUseCase implements GetPublicSpaceForEditUseCase<PublicSpaceDto> {

    private final PublicSpaceStore<? extends PublicSpaceView, PublicSpaceRequestDto> publicSpaceStore;

    @Override
    @Transactional(readOnly = true)
    public PublicSpaceDto execute(Long id, String locale) {
        return publicSpaceStore.findActiveById(id)
                .map(s -> PublicSpaceDto.fromWithTranslations(s, locale))
                .orElseThrow(() -> new ResourceNotFoundException("PublicSpace", id));
    }
}
