package com.modelcity.leisure.cityplaces.controller;

import com.modelcity.leisure.cityplaces.controller.model.CityPlaceDto;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceRequestDto;
import com.modelcity.leisure.cityplaces.usecase.CreateCityPlaceUseCase;
import com.modelcity.leisure.cityplaces.usecase.DeleteCityPlaceUseCase;
import com.modelcity.leisure.cityplaces.usecase.GetCityPlaceForEditUseCase;
import com.modelcity.leisure.cityplaces.usecase.GetCityPlaceUseCase;
import com.modelcity.leisure.cityplaces.usecase.GetCityPlacesUseCase;
import com.modelcity.leisure.cityplaces.usecase.UpdateCityPlaceUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CityPlaceControllerTest {

    @Mock GetCityPlacesUseCase<CityPlaceDto> getCityPlacesUseCase;
    @Mock GetCityPlaceUseCase<CityPlaceDto> getCityPlaceUseCase;
    @Mock GetCityPlaceForEditUseCase<CityPlaceDto> getCityPlaceForEditUseCase;
    @Mock CreateCityPlaceUseCase<CityPlaceDto, CityPlaceRequestDto> createCityPlaceUseCase;
    @Mock UpdateCityPlaceUseCase<CityPlaceDto, CityPlaceRequestDto> updateCityPlaceUseCase;
    @Mock DeleteCityPlaceUseCase deleteCityPlaceUseCase;

    DefaultCityPlaceController controller;

    @BeforeEach
    void setUp() {
        controller = new DefaultCityPlaceController(getCityPlacesUseCase, getCityPlaceUseCase,
                getCityPlaceForEditUseCase, createCityPlaceUseCase, updateCityPlaceUseCase, deleteCityPlaceUseCase);
    }

    @Test
    void getCityPlaces_delegatesWithResolvedLocale() {
        controller.getCityPlaces("MUSEUM", 1, Locale.forLanguageTag("fr-FR"));
        verify(getCityPlacesUseCase).execute("MUSEUM", 1, "fr");
    }

    @Test
    void getCityPlace_withoutTranslations_usesGetUseCase() {
        controller.getCityPlace(1L, null, Locale.ENGLISH);
        verify(getCityPlaceUseCase).execute(1L, "en");
        verify(getCityPlaceForEditUseCase, never()).execute(any(), any());
    }

    @Test
    void getCityPlace_withFullTranslations_usesForEditUseCase() {
        controller.getCityPlace(1L, "full", Locale.forLanguageTag("es"));
        verify(getCityPlaceForEditUseCase).execute(1L, "es");
        verify(getCityPlaceUseCase, never()).execute(any(), any());
    }

    @Test
    void getCityPlace_unknownLocale_fallsBackToDefault() {
        controller.getCityPlace(1L, null, Locale.forLanguageTag("de"));
        verify(getCityPlaceUseCase).execute(1L, "es");
    }

    @Test
    void createCityPlace_delegatesToUseCase() {
        CityPlaceRequestDto request = new CityPlaceRequestDto();
        controller.createCityPlace("sub-agent", request, Locale.ENGLISH);
        verify(createCityPlaceUseCase).execute("sub-agent", request, "en");
    }

    @Test
    void updateCityPlace_delegatesToUseCase() {
        CityPlaceRequestDto request = new CityPlaceRequestDto();
        controller.updateCityPlace(1L, "sub-agent", request, Locale.ENGLISH);
        verify(updateCityPlaceUseCase).execute(1L, "sub-agent", request, "en");
    }

    @Test
    void deleteCityPlace_delegatesToUseCase() {
        controller.deleteCityPlace(1L, "sub-agent");
        verify(deleteCityPlaceUseCase).execute(1L, "sub-agent");
    }
}
