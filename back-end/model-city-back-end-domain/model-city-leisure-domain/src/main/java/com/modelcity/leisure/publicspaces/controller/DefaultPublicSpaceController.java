package com.modelcity.leisure.publicspaces.controller;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.web.bind.annotation.RestController;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceDto;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceRequestDto;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceSummaryDto;
import com.modelcity.leisure.publicspaces.usecase.CreatePublicSpaceUseCase;
import com.modelcity.leisure.publicspaces.usecase.DeletePublicSpaceUseCase;
import com.modelcity.leisure.publicspaces.usecase.GetPublicSpaceForEditUseCase;
import com.modelcity.leisure.publicspaces.usecase.GetPublicSpaceUseCase;
import com.modelcity.leisure.publicspaces.usecase.GetPublicSpacesUseCase;
import com.modelcity.leisure.publicspaces.usecase.UpdatePublicSpaceUseCase;

/**
 * Default concrete {@link PublicSpaceController}, bound to the platform DTOs. The component-scanned platform
 * default; disabled at startup when a local deployment provides its own bean for the seam.
 */
@RestController
@ModelCityDisabledIfInherited
public class DefaultPublicSpaceController
        extends PublicSpaceController<PublicSpaceDto, PublicSpaceSummaryDto, PublicSpaceRequestDto> {

    public DefaultPublicSpaceController(
            GetPublicSpacesUseCase<PublicSpaceSummaryDto> getPublicSpacesUseCase,
            GetPublicSpaceUseCase<PublicSpaceDto> getPublicSpaceUseCase,
            GetPublicSpaceForEditUseCase<PublicSpaceDto> getPublicSpaceForEditUseCase,
            CreatePublicSpaceUseCase<PublicSpaceDto, PublicSpaceRequestDto> createPublicSpaceUseCase,
            UpdatePublicSpaceUseCase<PublicSpaceDto, PublicSpaceRequestDto> updatePublicSpaceUseCase,
            DeletePublicSpaceUseCase deletePublicSpaceUseCase) {
        super(getPublicSpacesUseCase, getPublicSpaceUseCase, getPublicSpaceForEditUseCase,
                createPublicSpaceUseCase, updatePublicSpaceUseCase, deletePublicSpaceUseCase);
    }
}
