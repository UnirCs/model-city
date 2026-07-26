package com.modelcity.leisure.publicspaces.controller;

import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceDto;
import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceRequestDto;
import com.modelcity.leisure.publicspaces.usecase.CreateReservableResourceUseCase;
import com.modelcity.leisure.publicspaces.usecase.DeleteReservableResourceUseCase;
import com.modelcity.leisure.publicspaces.usecase.GetReservableResourcesForEditUseCase;
import com.modelcity.leisure.publicspaces.usecase.GetReservableResourcesUseCase;
import com.modelcity.leisure.publicspaces.usecase.UpdateReservableResourceUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.Locale;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservableResourceControllerTest {

    @Mock GetReservableResourcesUseCase<ReservableResourceDto> getReservableResourcesUseCase;
    @Mock GetReservableResourcesForEditUseCase<ReservableResourceDto> getReservableResourcesForEditUseCase;
    @Mock CreateReservableResourceUseCase<ReservableResourceDto, ReservableResourceRequestDto> createReservableResourceUseCase;
    @Mock UpdateReservableResourceUseCase<ReservableResourceDto, ReservableResourceRequestDto> updateReservableResourceUseCase;
    @Mock DeleteReservableResourceUseCase deleteReservableResourceUseCase;

    DefaultReservableResourceController controller;

    @BeforeEach
    void setUp() {
        controller = new DefaultReservableResourceController(getReservableResourcesUseCase,
                getReservableResourcesForEditUseCase, createReservableResourceUseCase,
                updateReservableResourceUseCase, deleteReservableResourceUseCase);
    }

    @Test
    void getResources_withoutTranslations_usesGetUseCase() {
        PageRequest pageable = PageRequest.of(0, 4);
        controller.getResources(1L, pageable, null, Locale.ENGLISH);
        verify(getReservableResourcesUseCase).execute(1L, pageable, "en");
        verify(getReservableResourcesForEditUseCase, never()).execute(any(), any(), any());
    }

    @Test
    void getResources_withFullTranslations_usesForEditUseCase() {
        PageRequest pageable = PageRequest.of(0, 4);
        controller.getResources(1L, pageable, "full", Locale.ENGLISH);
        verify(getReservableResourcesForEditUseCase).execute(1L, pageable, "en");
        verify(getReservableResourcesUseCase, never()).execute(any(), any(), any());
    }

    @Test
    void createResource_delegatesToUseCase() {
        ReservableResourceRequestDto request = new ReservableResourceRequestDto();
        controller.createResource(1L, "sub-agent", request, Locale.ENGLISH);
        verify(createReservableResourceUseCase).execute(1L, "sub-agent", request, "en");
    }

    @Test
    void updateResource_delegatesToUseCase() {
        ReservableResourceRequestDto request = new ReservableResourceRequestDto();
        controller.updateResource(1L, 10L, "sub-agent", request, Locale.ENGLISH);
        verify(updateReservableResourceUseCase).execute(1L, 10L, "sub-agent", request, "en");
    }

    @Test
    void deleteResource_delegatesToUseCase() {
        controller.deleteResource(1L, 10L, "sub-agent");
        verify(deleteReservableResourceUseCase).execute(1L, 10L, "sub-agent");
    }
}
