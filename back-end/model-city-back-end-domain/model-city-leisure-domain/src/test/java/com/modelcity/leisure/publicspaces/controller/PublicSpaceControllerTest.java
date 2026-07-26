package com.modelcity.leisure.publicspaces.controller;

import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceDto;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceRequestDto;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceSummaryDto;
import com.modelcity.leisure.publicspaces.usecase.CreatePublicSpaceUseCase;
import com.modelcity.leisure.publicspaces.usecase.DeletePublicSpaceUseCase;
import com.modelcity.leisure.publicspaces.usecase.GetPublicSpaceForEditUseCase;
import com.modelcity.leisure.publicspaces.usecase.GetPublicSpaceUseCase;
import com.modelcity.leisure.publicspaces.usecase.GetPublicSpacesUseCase;
import com.modelcity.leisure.publicspaces.usecase.UpdatePublicSpaceUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicSpaceControllerTest {

    @Mock GetPublicSpacesUseCase<PublicSpaceSummaryDto> getPublicSpacesUseCase;
    @Mock GetPublicSpaceUseCase<PublicSpaceDto> getPublicSpaceUseCase;
    @Mock GetPublicSpaceForEditUseCase<PublicSpaceDto> getPublicSpaceForEditUseCase;
    @Mock CreatePublicSpaceUseCase<PublicSpaceDto, PublicSpaceRequestDto> createPublicSpaceUseCase;
    @Mock UpdatePublicSpaceUseCase<PublicSpaceDto, PublicSpaceRequestDto> updatePublicSpaceUseCase;
    @Mock DeletePublicSpaceUseCase deletePublicSpaceUseCase;

    DefaultPublicSpaceController controller;

    @BeforeEach
    void setUp() {
        controller = new DefaultPublicSpaceController(getPublicSpacesUseCase, getPublicSpaceUseCase,
                getPublicSpaceForEditUseCase, createPublicSpaceUseCase, updatePublicSpaceUseCase,
                deletePublicSpaceUseCase);
    }

    @Test
    void getPublicSpaces_delegatesWithResolvedLocale() {
        controller.getPublicSpaces(1, Locale.FRENCH);
        verify(getPublicSpacesUseCase).execute(1, "fr");
    }

    @Test
    void getPublicSpace_withoutTranslations_usesGetUseCase() {
        controller.getPublicSpace(1L, null, Locale.ENGLISH);
        verify(getPublicSpaceUseCase).execute(1L, "en");
        verify(getPublicSpaceForEditUseCase, never()).execute(any(), any());
    }

    @Test
    void getPublicSpace_withFullTranslations_usesForEditUseCase() {
        controller.getPublicSpace(1L, "full", Locale.ENGLISH);
        verify(getPublicSpaceForEditUseCase).execute(1L, "en");
        verify(getPublicSpaceUseCase, never()).execute(any(), any());
    }

    @Test
    void createPublicSpace_delegatesToUseCase() {
        PublicSpaceRequestDto request = new PublicSpaceRequestDto();
        controller.createPublicSpace("sub-agent", request, Locale.ENGLISH);
        verify(createPublicSpaceUseCase).execute("sub-agent", request, "en");
    }

    @Test
    void updatePublicSpace_delegatesToUseCase() {
        PublicSpaceRequestDto request = new PublicSpaceRequestDto();
        controller.updatePublicSpace(1L, "sub-agent", request, Locale.ENGLISH);
        verify(updatePublicSpaceUseCase).execute(1L, "sub-agent", request, "en");
    }

    @Test
    void deletePublicSpace_delegatesToUseCase() {
        controller.deletePublicSpace(1L, "sub-agent");
        verify(deletePublicSpaceUseCase).execute(1L, "sub-agent");
    }
}
