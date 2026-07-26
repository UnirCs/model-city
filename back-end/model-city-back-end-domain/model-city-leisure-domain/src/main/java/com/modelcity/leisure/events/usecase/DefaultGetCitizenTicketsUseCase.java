package com.modelcity.leisure.events.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;import com.modelcity.leisure.events.controller.model.EventRequestDto;

import com.modelcity.common.client.CoreClient;
import com.modelcity.leisure.events.controller.model.CitizenTicketDto;
import com.modelcity.leisure.events.store.model.EventTicketView;
import com.modelcity.leisure.events.store.model.EventView;
import com.modelcity.leisure.events.store.EventStore;
import com.modelcity.leisure.events.store.EventTicketStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Set;

/** Default {@link GetCitizenTicketsUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetCitizenTicketsUseCase implements GetCitizenTicketsUseCase<CitizenTicketDto> {

    private static final int PAGE_SIZE = 20;
    private static final Set<String> PRIVILEGED_ROLES = Set.of(
            "MODEL-CITY-PLATFORM-ADMIN", "MODEL-CITY-BACKOFFICE");

    private final EventTicketStore<? extends EventTicketView> eventTicketStore;
    private final EventStore<? extends EventView, EventRequestDto> eventStore;
    private final CoreClient coreClient;

    @Override
    @Transactional(readOnly = true)
    public Page<CitizenTicketDto> execute(String targetSub, String callerSub, int page, String period) {
        boolean privileged = isPrivileged(callerSub);
        if (!privileged && !targetSub.equals(callerSub)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your own tickets");
        }
        PageRequest pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("purchasedAt").descending());

        Page<? extends EventTicketView> ticketsPage;
        if (period != null) {
            LocalDate today = LocalDate.now();
            LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            ticketsPage = switch (period.toLowerCase()) {
                case "week" -> eventTicketStore.findByCitizenAndEventStartBetween(
                        targetSub, today.atStartOfDay(), weekEnd.plusDays(1).atStartOfDay(), pageable);
                case "future" -> eventTicketStore.findByCitizenAndEventStartAfter(
                        targetSub, weekEnd.plusDays(1).atStartOfDay(), pageable);
                case "past" -> eventTicketStore.findByCitizenAndEventStartBefore(
                        targetSub, today.atStartOfDay(), pageable);
                default -> eventTicketStore.findByCitizen(targetSub, pageable);
            };
        } else {
            ticketsPage = eventTicketStore.findByCitizen(targetSub, pageable);
        }

        return ticketsPage.map(t -> {
            EventView event = eventStore.findActiveById(t.getEventId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found for ticket"));
            return privileged ? CitizenTicketDto.privilegedView(t, event) : CitizenTicketDto.publicView(t, event);
        });
    }

    private boolean isPrivileged(String sub) {
        if (sub == null || sub.isBlank()) return false;
        try {
            String role = coreClient.getUserRole(sub);
            return role != null && PRIVILEGED_ROLES.contains(role);
        } catch (Exception ignored) {
            return false;
        }
    }
}
