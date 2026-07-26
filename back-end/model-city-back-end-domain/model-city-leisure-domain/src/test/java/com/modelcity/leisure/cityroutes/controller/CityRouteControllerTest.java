package com.modelcity.leisure.cityroutes.controller;

import com.modelcity.leisure.cityplaces.controller.model.CityPlaceDto;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceSummaryDto;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteDto;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteRequestDto;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteSummaryDto;
import com.modelcity.leisure.cityroutes.usecase.CreateCityRouteUseCase;
import com.modelcity.leisure.cityroutes.usecase.DeleteCityRouteUseCase;
import com.modelcity.leisure.cityroutes.usecase.GetCityRouteForEditUseCase;
import com.modelcity.leisure.cityroutes.usecase.GetCityRoutePlaceUseCase;
import com.modelcity.leisure.cityroutes.usecase.GetCityRoutePlacesUseCase;
import com.modelcity.leisure.cityroutes.usecase.GetCityRouteUseCase;
import com.modelcity.leisure.cityroutes.usecase.GetCityRoutesUseCase;
import com.modelcity.leisure.cityroutes.usecase.UpdateCityRouteUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.Locale;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CityRouteControllerTest {

    @Mock GetCityRoutesUseCase<CityRouteSummaryDto> getCityRoutesUseCase;
    @Mock GetCityRouteUseCase<CityRouteDto> getCityRouteUseCase;
    @Mock GetCityRouteForEditUseCase<CityRouteDto> getCityRouteForEditUseCase;
    @Mock GetCityRoutePlacesUseCase<CityPlaceSummaryDto> getCityRoutePlacesUseCase;
    @Mock GetCityRoutePlaceUseCase<CityPlaceDto> getCityRoutePlaceUseCase;
    @Mock CreateCityRouteUseCase<CityRouteDto, CityRouteRequestDto> createCityRouteUseCase;
    @Mock UpdateCityRouteUseCase<CityRouteDto, CityRouteRequestDto> updateCityRouteUseCase;
    @Mock DeleteCityRouteUseCase deleteCityRouteUseCase;

    DefaultCityRouteController controller;

    @BeforeEach
    void setUp() {
        controller = new DefaultCityRouteController(getCityRoutesUseCase, getCityRouteUseCase,
                getCityRouteForEditUseCase, getCityRoutePlacesUseCase, getCityRoutePlaceUseCase,
                createCityRouteUseCase, updateCityRouteUseCase, deleteCityRouteUseCase);
    }

    @Test
    void getCityRoutes_delegatesWithResolvedLocale() {
        controller.getCityRoutes(2, Locale.FRENCH);
        verify(getCityRoutesUseCase).execute(2, "fr");
    }

    @Test
    void getCityRoute_withoutTranslations_usesGetUseCase() {
        controller.getCityRoute(1L, null, Locale.ENGLISH);
        verify(getCityRouteUseCase).execute(1L, "en");
        verify(getCityRouteForEditUseCase, never()).execute(any(), any());
    }

    @Test
    void getCityRoute_withFullTranslations_usesForEditUseCase() {
        controller.getCityRoute(1L, "full", Locale.ENGLISH);
        verify(getCityRouteForEditUseCase).execute(1L, "en");
        verify(getCityRouteUseCase, never()).execute(any(), any());
    }

    @Test
    void getCityRoutePlaces_delegatesToUseCase() {
        PageRequest pageable = PageRequest.of(0, 10);
        controller.getCityRoutePlaces(1L, pageable, Locale.ENGLISH);
        verify(getCityRoutePlacesUseCase).execute(1L, pageable, "en");
    }

    @Test
    void getCityRoutePlace_delegatesToUseCase() {
        controller.getCityRoutePlace(1L, 5L, Locale.ENGLISH);
        verify(getCityRoutePlaceUseCase).execute(1L, 5L, "en");
    }

    @Test
    void createCityRoute_delegatesToUseCase() {
        CityRouteRequestDto request = new CityRouteRequestDto();
        controller.createCityRoute("sub-agent", request, Locale.ENGLISH);
        verify(createCityRouteUseCase).execute("sub-agent", request, "en");
    }

    @Test
    void updateCityRoute_delegatesToUseCase() {
        CityRouteRequestDto request = new CityRouteRequestDto();
        controller.updateCityRoute(1L, "sub-agent", request, Locale.ENGLISH);
        verify(updateCityRouteUseCase).execute(1L, "sub-agent", request, "en");
    }

    @Test
    void deleteCityRoute_delegatesToUseCase() {
        controller.deleteCityRoute(1L, "sub-agent");
        verify(deleteCityRouteUseCase).execute(1L, "sub-agent");
    }
}
