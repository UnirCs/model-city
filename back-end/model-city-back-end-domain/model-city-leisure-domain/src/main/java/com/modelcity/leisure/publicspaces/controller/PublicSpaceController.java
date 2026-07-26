package com.modelcity.leisure.publicspaces.controller;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.common.i18n.SupportedLocale;
import com.modelcity.common.security.ModelCityAccess;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceDto;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceRequestDto;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceSummaryDto;
import com.modelcity.leisure.publicspaces.usecase.CreatePublicSpaceUseCase;
import com.modelcity.leisure.publicspaces.usecase.DeletePublicSpaceUseCase;
import com.modelcity.leisure.publicspaces.usecase.GetPublicSpaceForEditUseCase;
import com.modelcity.leisure.publicspaces.usecase.GetPublicSpaceUseCase;
import com.modelcity.leisure.publicspaces.usecase.GetPublicSpacesUseCase;
import com.modelcity.leisure.publicspaces.usecase.UpdatePublicSpaceUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

/**
 * Endpoints to browse and manage public spaces (sport centres, libraries...).
 *
 * <p>Overridable base controller (abstract): the platform registers {@link DefaultPublicSpaceController} as
 * the default bean. A local deployment overrides by declaring its own {@code @RestController} that
 * {@code extends PublicSpaceController}; the default then backs off.
 */
@Slf4j
@RestController
@RequestMapping("/public-spaces")
@RequiredArgsConstructor
@ModelCityExtensionPoint
public abstract class PublicSpaceController<
        T extends PublicSpaceDto,
        S extends PublicSpaceSummaryDto,
        R extends PublicSpaceRequestDto> {

    protected final GetPublicSpacesUseCase<S> getPublicSpacesUseCase;
    protected final GetPublicSpaceUseCase<T> getPublicSpaceUseCase;
    protected final GetPublicSpaceForEditUseCase<T> getPublicSpaceForEditUseCase;
    protected final CreatePublicSpaceUseCase<T, R> createPublicSpaceUseCase;
    protected final UpdatePublicSpaceUseCase<T, R> updatePublicSpaceUseCase;
    protected final DeletePublicSpaceUseCase deletePublicSpaceUseCase;

    /** Lists active public spaces paginated (page size 6). */
    @GetMapping
    public Page<S> getPublicSpaces(@RequestParam(defaultValue = "0") int page, Locale locale) {
        log.debug("GET /public-spaces page={}", page);
        return getPublicSpacesUseCase.execute(page, SupportedLocale.from(locale).code());
    }

    /** Returns the detail of a single public space. {@code ?translations=full} returns every locale (admin editing). */
    @GetMapping("/{id}")
    public T getPublicSpace(
            @PathVariable Long id,
            @RequestParam(required = false) String translations,
            Locale locale) {
        log.debug("GET /public-spaces/{} translations={}", id, translations);
        String code = SupportedLocale.from(locale).code();
        return "full".equals(translations)
                ? getPublicSpaceForEditUseCase.execute(id, code)
                : getPublicSpaceUseCase.execute(id, code);
    }

    /** Creates a new public space. Admin only. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ModelCityAccess.PlatformAdmin
    public T createPublicSpace(
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid R request,
            Locale locale) {
        log.debug("POST /public-spaces sub={}", sub);
        return createPublicSpaceUseCase.execute(sub, request, SupportedLocale.from(locale).code());
    }

    /** Updates an existing public space. Admin only. */
    @PutMapping("/{id}")
    @ModelCityAccess.PlatformAdmin
    public T updatePublicSpace(
            @PathVariable Long id,
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid R request,
            Locale locale) {
        log.debug("PUT /public-spaces/{} sub={}", id, sub);
        return updatePublicSpaceUseCase.execute(id, sub, request, SupportedLocale.from(locale).code());
    }

    /** Soft-deletes a public space (and its resources). Admin only. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ModelCityAccess.PlatformAdmin
    public void deletePublicSpace(
            @PathVariable Long id,
            @RequestHeader("X-Auth-Sub") String sub) {
        log.debug("DELETE /public-spaces/{} sub={}", id, sub);
        deletePublicSpaceUseCase.execute(id, sub);
    }
}
