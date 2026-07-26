package com.modelcity.engagement.alerts.controller;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.engagement.alerts.controller.model.SecurityAlertDto;
import com.modelcity.engagement.alerts.controller.model.SecurityAlertRequestDto;
import com.modelcity.engagement.alerts.usecase.CreateSecurityAlertUseCase;
import com.modelcity.engagement.alerts.usecase.DeleteSecurityAlertUseCase;
import com.modelcity.engagement.alerts.usecase.GetSecurityAlertsUseCase;
import com.modelcity.common.i18n.SupportedLocale;
import com.modelcity.common.security.ModelCityAccess;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

/**
 * Endpoints for managing and querying citizen security alerts.
 *
 * <p>Overridable base controller (abstract): the platform registers {@link DefaultSecurityAlertController}
 * as the default bean. A local deployment overrides by declaring its own {@code @RestController} that
 * {@code extends SecurityAlertController}; the default then backs off.
 */
@Slf4j
@RestController
@RequestMapping("/security-alerts")
@RequiredArgsConstructor
@ModelCityExtensionPoint
public abstract class SecurityAlertController<T extends SecurityAlertDto, R extends SecurityAlertRequestDto> {

    protected final GetSecurityAlertsUseCase<T> getSecurityAlertsUseCase;
    protected final CreateSecurityAlertUseCase<T, R> createSecurityAlertUseCase;
    protected final DeleteSecurityAlertUseCase deleteSecurityAlertUseCase;

    /** Returns paginated active (non-expired) security alerts. Public endpoint. */
    @GetMapping
    public Page<T> getSecurityAlerts(@RequestParam(defaultValue = "0") int page, Locale locale) {
        log.debug("GET /security-alerts page={}", page);
        return getSecurityAlertsUseCase.execute(page, SupportedLocale.from(locale).code());
    }

    /** Creates a new security alert. Admin, backoffice, operator or mobility agent only. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ModelCityAccess.PlatformAdmin
    @ModelCityAccess.BackOffice
    @ModelCityAccess.Operator
    @ModelCityAccess.MobilityAgent
    public T createSecurityAlert(
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid R request,
            Locale locale) {
        log.debug("POST /security-alerts severity={} sub={}", request.getSeverity(), sub);
        return createSecurityAlertUseCase.execute(sub, request, SupportedLocale.from(locale).code());
    }

    /** Deletes a security alert by id. Admin, backoffice, operator or mobility agent only. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ModelCityAccess.PlatformAdmin
    @ModelCityAccess.BackOffice
    @ModelCityAccess.Operator
    @ModelCityAccess.MobilityAgent
    public void deleteSecurityAlert(
            @PathVariable Long id,
            @RequestHeader("X-Auth-Sub") String sub) {
        log.debug("DELETE /security-alerts/{} sub={}", id, sub);
        deleteSecurityAlertUseCase.execute(id, sub);
    }
}
