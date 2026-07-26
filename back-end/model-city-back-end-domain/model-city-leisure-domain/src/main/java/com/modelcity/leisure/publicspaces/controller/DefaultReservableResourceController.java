package com.modelcity.leisure.publicspaces.controller;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.web.bind.annotation.RestController;
import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceDto;
import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceRequestDto;
import com.modelcity.leisure.publicspaces.usecase.CreateReservableResourceUseCase;
import com.modelcity.leisure.publicspaces.usecase.DeleteReservableResourceUseCase;
import com.modelcity.leisure.publicspaces.usecase.GetReservableResourcesForEditUseCase;
import com.modelcity.leisure.publicspaces.usecase.GetReservableResourcesUseCase;
import com.modelcity.leisure.publicspaces.usecase.UpdateReservableResourceUseCase;

/**
 * Default concrete {@link ReservableResourceController}, bound to the platform DTOs. The component-scanned
 * platform default; disabled at startup when a local deployment provides its own bean for the seam.
 */
@RestController
@ModelCityDisabledIfInherited
public class DefaultReservableResourceController
        extends ReservableResourceController<ReservableResourceDto, ReservableResourceRequestDto> {

    public DefaultReservableResourceController(
            GetReservableResourcesUseCase<ReservableResourceDto> getReservableResourcesUseCase,
            GetReservableResourcesForEditUseCase<ReservableResourceDto> getReservableResourcesForEditUseCase,
            CreateReservableResourceUseCase<ReservableResourceDto, ReservableResourceRequestDto> createReservableResourceUseCase,
            UpdateReservableResourceUseCase<ReservableResourceDto, ReservableResourceRequestDto> updateReservableResourceUseCase,
            DeleteReservableResourceUseCase deleteReservableResourceUseCase) {
        super(getReservableResourcesUseCase, getReservableResourcesForEditUseCase,
                createReservableResourceUseCase, updateReservableResourceUseCase, deleteReservableResourceUseCase);
    }
}
