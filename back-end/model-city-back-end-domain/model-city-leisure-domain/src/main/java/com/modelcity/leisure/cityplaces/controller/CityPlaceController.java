package com.modelcity.leisure.cityplaces.controller;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.common.i18n.SupportedLocale;
import com.modelcity.common.security.ModelCityAccess;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceDto;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceRequestDto;
import com.modelcity.leisure.cityplaces.usecase.CreateCityPlaceUseCase;
import com.modelcity.leisure.cityplaces.usecase.DeleteCityPlaceUseCase;
import com.modelcity.leisure.cityplaces.usecase.GetCityPlaceForEditUseCase;
import com.modelcity.leisure.cityplaces.usecase.GetCityPlaceUseCase;
import com.modelcity.leisure.cityplaces.usecase.GetCityPlacesUseCase;
import com.modelcity.leisure.cityplaces.usecase.UpdateCityPlaceUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

/**
 * Endpoints for managing and browsing city places.
 *
 * <p>Overridable base controller (abstract): the platform registers {@link DefaultCityPlaceController} as the
 * default bean. A local deployment overrides by declaring its own {@code @RestController} that
 * {@code extends CityPlaceController}; the default then backs off.
 */
@Slf4j
@RestController
@RequestMapping("/city-places")
@RequiredArgsConstructor
@ModelCityExtensionPoint
public abstract class CityPlaceController<T extends CityPlaceDto, R extends CityPlaceRequestDto> {

    protected final GetCityPlacesUseCase<T> getCityPlacesUseCase;
    protected final GetCityPlaceUseCase<T> getCityPlaceUseCase;
    protected final GetCityPlaceForEditUseCase<T> getCityPlaceForEditUseCase;
    protected final CreateCityPlaceUseCase<T, R> createCityPlaceUseCase;
    protected final UpdateCityPlaceUseCase<T, R> updateCityPlaceUseCase;
    protected final DeleteCityPlaceUseCase deleteCityPlaceUseCase;

    /** Lists city places paginated (page size 6), optionally filtered by category. */
    @GetMapping
    public Page<T> getCityPlaces(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            Locale locale) {
        log.debug("GET /city-places category={} page={}", category, page);
        return getCityPlacesUseCase.execute(category, page, SupportedLocale.from(locale).code());
    }

    /** Returns the full detail of a single city place. {@code ?translations=full} returns every locale (admin editing). */
    @GetMapping("/{id}")
    public T getCityPlace(
            @PathVariable Long id,
            @RequestParam(required = false) String translations,
            Locale locale) {
        log.debug("GET /city-places/{} translations={}", id, translations);
        String code = SupportedLocale.from(locale).code();
        return "full".equals(translations)
                ? getCityPlaceForEditUseCase.execute(id, code)
                : getCityPlaceUseCase.execute(id, code);
    }

    /** Creates a new city place. Admin or backoffice only. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ModelCityAccess.PlatformAdmin
    @ModelCityAccess.BackOffice
    public T createCityPlace(
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid R request,
            Locale locale) {
        log.debug("POST /city-places sub={}", sub);
        return createCityPlaceUseCase.execute(sub, request, SupportedLocale.from(locale).code());
    }

    /** Fully replaces an existing city place. Admin or backoffice only. */
    @PutMapping("/{id}")
    @ModelCityAccess.PlatformAdmin
    @ModelCityAccess.BackOffice
    public T updateCityPlace(
            @PathVariable Long id,
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid R request,
            Locale locale) {
        log.debug("PUT /city-places/{} sub={}", id, sub);
        return updateCityPlaceUseCase.execute(id, sub, request, SupportedLocale.from(locale).code());
    }

    /** Deletes a city place. Admin or backoffice only. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ModelCityAccess.PlatformAdmin
    @ModelCityAccess.BackOffice
    public void deleteCityPlace(
            @PathVariable Long id,
            @RequestHeader("X-Auth-Sub") String sub) {
        log.debug("DELETE /city-places/{} sub={}", id, sub);
        deleteCityPlaceUseCase.execute(id, sub);
    }
}
