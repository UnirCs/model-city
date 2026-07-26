package com.modelcity.leisure.publicspaces.store;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;

import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceRequestDto;
import com.modelcity.leisure.publicspaces.model.ReservableResourceView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/** Persistence port for reservable resources. */
@ModelCityExtensionPoint
public interface ReservableResourceStore<T extends ReservableResourceView, R extends ReservableResourceRequestDto> {

    Optional<T> findActiveByIdAndPublicSpace(Long id, Long publicSpaceId);

    Page<T> findActiveByPublicSpace(Long publicSpaceId, Pageable pageable);

    T create(Long publicSpaceId, R request);

    T update(Long id, Long publicSpaceId, R request);

    void softDelete(Long id, Long publicSpaceId);

    /** Cascades a soft-delete to all active resources of the given public space. */
    void softDeleteByPublicSpace(Long publicSpaceId);
}
