package com.modelcity.leisure.publicspaces.store;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;

import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceRequestDto;
import com.modelcity.leisure.publicspaces.model.PublicSpaceView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/** Persistence port for public spaces. */
@ModelCityExtensionPoint
public interface PublicSpaceStore<T extends PublicSpaceView, R extends PublicSpaceRequestDto> {

    Page<T> findActive(Pageable pageable);

    Optional<T> findActiveById(Long id);

    /** Builds and persists a new active public space from the request. */
    T create(R request);

    /** Applies the request onto the existing active space (assumed to exist) and persists it. */
    T update(Long id, R request);

    /** Soft-deletes the public space (assumed to exist). */
    void softDelete(Long id);
}
