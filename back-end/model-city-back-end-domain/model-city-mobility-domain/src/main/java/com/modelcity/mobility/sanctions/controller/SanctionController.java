package com.modelcity.mobility.sanctions.controller;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.common.security.ModelCityAccess;
import com.modelcity.mobility.sanctions.controller.model.SanctionDto;
import com.modelcity.mobility.sanctions.controller.model.SanctionRequestDto;
import com.modelcity.mobility.sanctions.controller.model.SanctionSummaryDto;
import com.modelcity.mobility.sanctions.usecase.CreateSanctionUseCase;
import com.modelcity.mobility.sanctions.usecase.GetSanctionUseCase;
import com.modelcity.mobility.sanctions.usecase.GetSanctionsUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

/**
 * Admin/mobility-agent endpoints for parking sanctions.
 *
 * <p>Overridable base controller (abstract): the platform registers {@link DefaultSanctionController} as the
 * default bean. A local deployment overrides by declaring its own {@code @RestController} that
 * {@code extends SanctionController}; the default then backs off.
 */
@Slf4j
@RestController
@RequestMapping("/sanctions")
@RequiredArgsConstructor
@ModelCityExtensionPoint
public abstract class SanctionController<T extends SanctionDto, R extends SanctionRequestDto, S extends SanctionSummaryDto> {

    protected final CreateSanctionUseCase<T, R> createSanctionUseCase;
    protected final GetSanctionsUseCase<S> getSanctionsUseCase;
    protected final GetSanctionUseCase<T> getSanctionUseCase;

    /** Issues a new sanction. Admin or mobility agent only. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ModelCityAccess.PlatformAdmin
    @ModelCityAccess.MobilityAgent
    public T createSanction(
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid R request) {
        log.debug("POST /sanctions sub={} plate={}", sub, request.getLicensePlate());
        return createSanctionUseCase.execute(sub, request);
    }

    /** Lists sanctions (no image). Admin or mobility agent only. */
    @GetMapping
    @ModelCityAccess.PlatformAdmin
    @ModelCityAccess.MobilityAgent
    public Page<S> getSanctions(
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestParam(required = false) String licensePlate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(defaultValue = "0") int page) {
        log.debug("GET /sanctions sub={} plate={} from={} to={} page={}", sub, licensePlate, from, to, page);
        return getSanctionsUseCase.execute(licensePlate, from, to, page);
    }

    /** Returns the full sanction including the evidence image. Admin or mobility agent only. */
    @GetMapping("/{id}")
    @ModelCityAccess.PlatformAdmin
    @ModelCityAccess.MobilityAgent
    public T getSanction(
            @PathVariable Long id,
            @RequestHeader("X-Auth-Sub") String sub) {
        log.debug("GET /sanctions/{} sub={}", id, sub);
        return getSanctionUseCase.execute(id);
    }
}
