package com.modelcity.leisure.events.controller;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.events.controller.model.CitizenTicketDto;
import com.modelcity.leisure.events.usecase.GetCitizenTicketsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes ticket history scoped to a specific citizen.
 *
 * <p>Overridable base controller (abstract): the platform registers {@link DefaultCitizenTicketController} as
 * the default bean. A local deployment overrides by declaring its own {@code @RestController} that
 * {@code extends CitizenTicketController}; the default then backs off.
 */
@Slf4j
@RestController
@RequestMapping("/users/{userId}/tickets")
@RequiredArgsConstructor
@ModelCityExtensionPoint
public abstract class CitizenTicketController<T extends CitizenTicketDto> {

    protected final GetCitizenTicketsUseCase<T> getCitizenTicketsUseCase;

    /**
     * Returns the paginated ticket history for the given user.
     * Citizens may only query their own {@code userId}; admin and backoffice may query any user.
     */
    @GetMapping
    public Page<T> getTickets(
            @PathVariable String userId,
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String period) {
        log.debug("GET /users/{}/tickets callerSub={} page={} period={}", userId, sub, page, period);
        return getCitizenTicketsUseCase.execute(userId, sub, page, period);
    }
}
