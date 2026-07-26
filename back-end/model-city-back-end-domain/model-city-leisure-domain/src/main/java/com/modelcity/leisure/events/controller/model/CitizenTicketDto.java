package com.modelcity.leisure.events.controller.model;

import com.modelcity.leisure.events.store.model.EventTicketView;
import com.modelcity.leisure.events.store.model.EventView;
import com.modelcity.leisure.events.repository.model.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Ticket representation for citizen view, includes event details.
 *
 * <p>Extensible DTO (plain class, not a {@code record}): a city may subclass it and work with the subtype
 * through the generic seam {@code GetCitizenTicketsUseCase<T extends CitizenTicketDto>}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CitizenTicketDto {

    private Long id;
    private Long eventId;
    private BigDecimal pricePaid;
    private String currency;
    private TicketStatus status;
    private LocalDateTime purchasedAt;
    private LocalDateTime refundedAt;
    private String stripeCheckoutSessionId;
    private String citizenSub;
    private String citizenName;
    private String eventName;
    private LocalDate eventStart;
    private String eventPhoto;

    /** Privileged view, includes citizen identification. */
    public static CitizenTicketDto privilegedView(EventTicketView t, EventView e) {
        return new CitizenTicketDto(t.getId(), t.getEventId(), t.getPricePaid(), t.getCurrency(),
                t.getStatus(), t.getPurchasedAt(), t.getRefundedAt(),
                t.getStripeCheckoutSessionId(),
                t.getCitizenSub(), t.getCitizenName(),
                e.getName(), e.getStartsAt().toLocalDate(), e.getPhotoUrl1());
    }

    /** Public view, no citizen identification. */
    public static CitizenTicketDto publicView(EventTicketView t, EventView e) {
        return new CitizenTicketDto(t.getId(), t.getEventId(), t.getPricePaid(), t.getCurrency(),
                t.getStatus(), t.getPurchasedAt(), t.getRefundedAt(),
                t.getStripeCheckoutSessionId(),
                null, null,
                e.getName(), e.getStartsAt().toLocalDate(), e.getPhotoUrl1());
    }
}
