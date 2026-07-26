package com.modelcity.leisure.cityroutes.controller;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.common.i18n.SupportedLocale;
import com.modelcity.common.security.ModelCityAccess;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

/**
 * Public and administrative endpoints for city routes.
 *
 * <p>Overridable base controller (abstract): the platform registers {@link DefaultCityRouteController} as the
 * default bean. A local deployment overrides by declaring its own {@code @RestController} that
 * {@code extends CityRouteController}; the default then backs off.
 */
@Slf4j
@RestController
@RequestMapping("/city-routes")
@RequiredArgsConstructor
@ModelCityExtensionPoint
public abstract class CityRouteController<
        T extends CityRouteDto,
        S extends CityRouteSummaryDto,
        R extends CityRouteRequestDto,
        P extends CityPlaceDto,
        PS extends CityPlaceSummaryDto> {

    protected final GetCityRoutesUseCase<S> getCityRoutesUseCase;
    protected final GetCityRouteUseCase<T> getCityRouteUseCase;
    protected final GetCityRouteForEditUseCase<T> getCityRouteForEditUseCase;
    protected final GetCityRoutePlacesUseCase<PS> getCityRoutePlacesUseCase;
    protected final GetCityRoutePlaceUseCase<P> getCityRoutePlaceUseCase;
    protected final CreateCityRouteUseCase<T, R> createCityRouteUseCase;
    protected final UpdateCityRouteUseCase<T, R> updateCityRouteUseCase;
    protected final DeleteCityRouteUseCase deleteCityRouteUseCase;

    /** Lists city routes paginated (page size 3). */
    @GetMapping
    public Page<S> getCityRoutes(@RequestParam(defaultValue = "0") int page, Locale locale) {
        log.debug("GET /city-routes page={}", page);
        return getCityRoutesUseCase.execute(page, SupportedLocale.from(locale).code());
    }

    /** Returns the detail of a single city route. {@code ?translations=full} returns every locale (admin editing). */
    @GetMapping("/{id}")
    public T getCityRoute(
            @PathVariable Long id,
            @RequestParam(required = false) String translations,
            Locale locale) {
        log.debug("GET /city-routes/{} translations={}", id, translations);
        String code = SupportedLocale.from(locale).code();
        return "full".equals(translations)
                ? getCityRouteForEditUseCase.execute(id, code)
                : getCityRouteUseCase.execute(id, code);
    }

    /** Returns the ordered list of places associated with a route. */
    @GetMapping("/{id}/city-places")
    public Page<PS> getCityRoutePlaces(
            @PathVariable Long id,
            @PageableDefault(size = 10) Pageable pageable,
            Locale locale) {
        log.debug("GET /city-routes/{}/city-places", id);
        return getCityRoutePlacesUseCase.execute(id, pageable, SupportedLocale.from(locale).code());
    }

    /** Returns the detail of a place that belongs to a route. */
    @GetMapping("/{id}/city-places/{placeId}")
    public P getCityRoutePlace(
            @PathVariable Long id,
            @PathVariable Long placeId,
            Locale locale) {
        log.debug("GET /city-routes/{}/city-places/{}", id, placeId);
        return getCityRoutePlaceUseCase.execute(id, placeId, SupportedLocale.from(locale).code());
    }

    /** Creates a new city route. Admin or backoffice only. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ModelCityAccess.PlatformAdmin
    @ModelCityAccess.BackOffice
    public T createCityRoute(
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid R request,
            Locale locale) {
        log.debug("POST /city-routes sub={}", sub);
        return createCityRouteUseCase.execute(sub, request, SupportedLocale.from(locale).code());
    }

    /** Fully replaces an existing city route. Admin or backoffice only. */
    @PutMapping("/{id}")
    @ModelCityAccess.PlatformAdmin
    @ModelCityAccess.BackOffice
    public T updateCityRoute(
            @PathVariable Long id,
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid R request,
            Locale locale) {
        log.debug("PUT /city-routes/{} sub={}", id, sub);
        return updateCityRouteUseCase.execute(id, sub, request, SupportedLocale.from(locale).code());
    }

    /** Deletes a city route. Admin or backoffice only. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ModelCityAccess.PlatformAdmin
    @ModelCityAccess.BackOffice
    public void deleteCityRoute(
            @PathVariable Long id,
            @RequestHeader("X-Auth-Sub") String sub) {
        log.debug("DELETE /city-routes/{} sub={}", id, sub);
        deleteCityRouteUseCase.execute(id, sub);
    }
}
