package com.modelcity.leisure.events.controller.model;

import com.modelcity.leisure.events.store.model.EventTicketView;
import com.modelcity.leisure.events.repository.model.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Public-facing representation of a ticket.
 * Citizen identification fields are only populated for the buyer or for staff callers.
 *
 * <p>Extensible DTO (plain class, not a {@code record}): a city may subclass it and work with the subtype
 * through the generic seams {@code PurchaseTicketUseCase<T extends TicketDto, ...>} and
 * {@code GetEventTicketsUseCase<T extends TicketDto>}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketDto {

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

    /** Privileged view, includes citizen identification. */
    public static TicketDto privilegedView(EventTicketView t) {
        return new TicketDto(t.getId(), t.getEventId(), t.getPricePaid(), t.getCurrency(),
                t.getStatus(), t.getPurchasedAt(), t.getRefundedAt(),
                t.getStripeCheckoutSessionId(),
                t.getCitizenSub(), t.getCitizenName());
    }

    /** Public view, no citizen identification. */
    public static TicketDto publicView(EventTicketView t) {
        return new TicketDto(t.getId(), t.getEventId(), t.getPricePaid(), t.getCurrency(),
                t.getStatus(), t.getPurchasedAt(), t.getRefundedAt(),
                t.getStripeCheckoutSessionId(),
                null, null);
    }
}
