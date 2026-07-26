package com.modelcity.engagement.trails.controller;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.engagement.trails.usecase.GetSystemTrailsUseCase;
import com.modelcity.common.trails.SystemTrailDto;
import com.modelcity.common.trails.SystemTrailQuery;
import com.modelcity.common.security.ModelCityAccess;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

/**
 * Admin-only read access to the engagement vertical's system trails (audit log).
 *
 * <p>Overridable base controller (abstract): the platform registers {@link DefaultSystemTrailController} as
 * the default bean. A local deployment overrides by declaring its own {@code @RestController} that
 * {@code extends SystemTrailController}; the default then backs off.
 */
@Slf4j
@RestController
@RequestMapping("/system-trails")
@RequiredArgsConstructor
@ModelCityExtensionPoint
public abstract class SystemTrailController {

    protected final GetSystemTrailsUseCase getSystemTrailsUseCase;

    @GetMapping
    @ModelCityAccess.PlatformAdmin
    public Page<SystemTrailDto> list(
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String responsibleUserId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(defaultValue = "0") int page) {
        log.debug("GET /system-trails [engagement] sub={} type={} user={} from={} to={} page={}",
                sub, eventType, responsibleUserId, from, to, page);
        return getSystemTrailsUseCase.execute(new SystemTrailQuery(eventType, responsibleUserId, from, to), page);
    }
}
