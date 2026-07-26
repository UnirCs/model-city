package com.modelcity.leisure.publicspaces.controller;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.common.i18n.SupportedLocale;
import com.modelcity.common.security.ModelCityAccess;
import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceDto;
import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceRequestDto;
import com.modelcity.leisure.publicspaces.usecase.CreateReservableResourceUseCase;
import com.modelcity.leisure.publicspaces.usecase.DeleteReservableResourceUseCase;
import com.modelcity.leisure.publicspaces.usecase.GetReservableResourcesForEditUseCase;
import com.modelcity.leisure.publicspaces.usecase.GetReservableResourcesUseCase;
import com.modelcity.leisure.publicspaces.usecase.UpdateReservableResourceUseCase;
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
 * Endpoints to browse and manage the reservable resources of a public space.
 *
 * <p>Overridable base controller (abstract): the platform registers {@link DefaultReservableResourceController}
 * as the default bean. A local deployment overrides by declaring its own {@code @RestController} that
 * {@code extends ReservableResourceController}; the default then backs off.
 */
@Slf4j
@RestController
@RequestMapping("/public-spaces/{publicSpaceId}/resources")
@RequiredArgsConstructor
@ModelCityExtensionPoint
public abstract class ReservableResourceController<
        T extends ReservableResourceDto,
        R extends ReservableResourceRequestDto> {

    protected final GetReservableResourcesUseCase<T> getReservableResourcesUseCase;
    protected final GetReservableResourcesForEditUseCase<T> getReservableResourcesForEditUseCase;
    protected final CreateReservableResourceUseCase<T, R> createReservableResourceUseCase;
    protected final UpdateReservableResourceUseCase<T, R> updateReservableResourceUseCase;
    protected final DeleteReservableResourceUseCase deleteReservableResourceUseCase;

    /** Lists the active reservable resources of a public space. */
    @GetMapping
    public Page<T> getResources(
            @PathVariable Long publicSpaceId,
            @PageableDefault(size = 4) Pageable pageable,
            @RequestParam(required = false) String translations,
            Locale locale) {
        log.debug("GET /public-spaces/{}/resources translations={}", publicSpaceId, translations);
        String code = SupportedLocale.from(locale).code();
        return "full".equals(translations)
                ? getReservableResourcesForEditUseCase.execute(publicSpaceId, pageable, code)
                : getReservableResourcesUseCase.execute(publicSpaceId, pageable, code);
    }

    /** Creates a new reservable resource. Operator or admin. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ModelCityAccess.PlatformAdmin
    @ModelCityAccess.Operator
    public T createResource(
            @PathVariable Long publicSpaceId,
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid R request,
            Locale locale) {
        log.debug("POST /public-spaces/{}/resources sub={}", publicSpaceId, sub);
        return createReservableResourceUseCase.execute(publicSpaceId, sub, request, SupportedLocale.from(locale).code());
    }

    /** Updates an existing reservable resource. Operator or admin. */
    @PutMapping("/{resourceId}")
    @ModelCityAccess.PlatformAdmin
    @ModelCityAccess.Operator
    public T updateResource(
            @PathVariable Long publicSpaceId,
            @PathVariable Long resourceId,
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid R request,
            Locale locale) {
        log.debug("PUT /public-spaces/{}/resources/{} sub={}", publicSpaceId, resourceId, sub);
        return updateReservableResourceUseCase.execute(publicSpaceId, resourceId, sub, request, SupportedLocale.from(locale).code());
    }

    /** Soft-deletes a reservable resource. Operator or admin. */
    @DeleteMapping("/{resourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ModelCityAccess.PlatformAdmin
    @ModelCityAccess.Operator
    public void deleteResource(
            @PathVariable Long publicSpaceId,
            @PathVariable Long resourceId,
            @RequestHeader("X-Auth-Sub") String sub) {
        log.debug("DELETE /public-spaces/{}/resources/{} sub={}", publicSpaceId, resourceId, sub);
        deleteReservableResourceUseCase.execute(publicSpaceId, resourceId, sub);
    }
}
