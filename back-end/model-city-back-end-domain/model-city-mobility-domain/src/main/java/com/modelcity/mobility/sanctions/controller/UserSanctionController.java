package com.modelcity.mobility.sanctions.controller;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.mobility.sanctions.controller.model.SanctionDto;
import com.modelcity.mobility.sanctions.controller.model.SanctionSummaryDto;
import com.modelcity.mobility.sanctions.usecase.GetUserSanctionUseCase;
import com.modelcity.mobility.sanctions.usecase.GetUserSanctionsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * Citizen-facing sanction endpoints. Caller sub must match the path user id.
 *
 * <p>Overridable base controller (abstract): the platform registers {@link DefaultUserSanctionController} as
 * the default bean. A local deployment overrides by declaring its own {@code @RestController} that
 * {@code extends UserSanctionController}; the default then backs off.
 */
@Slf4j
@RestController
@RequestMapping("/users/{userId}/sanctions")
@RequiredArgsConstructor
@ModelCityExtensionPoint
public abstract class UserSanctionController<T extends SanctionDto, S extends SanctionSummaryDto> {

    protected final GetUserSanctionsUseCase<S> getUserSanctionsUseCase;
    protected final GetUserSanctionUseCase<T> getUserSanctionUseCase;

    /** Lists sanctions affecting the caller's cars (no image). */
    @GetMapping
    public Page<S> getUserSanctions(
            @PathVariable String userId,
            @RequestHeader("X-Auth-Sub") String sub,
            @PageableDefault(size = 10) Pageable pageable) {
        log.debug("GET /users/{}/sanctions sub={}", userId, sub);
        return getUserSanctionsUseCase.execute(userId, sub, pageable);
    }

    /** Returns the full sanction (with image) only if it belongs to the caller. */
    @GetMapping("/{id}")
    public T getUserSanction(
            @PathVariable String userId,
            @PathVariable Long id,
            @RequestHeader("X-Auth-Sub") String sub) {
        log.debug("GET /users/{}/sanctions/{} sub={}", userId, id, sub);
        return getUserSanctionUseCase.execute(userId, sub, id);
    }
}
