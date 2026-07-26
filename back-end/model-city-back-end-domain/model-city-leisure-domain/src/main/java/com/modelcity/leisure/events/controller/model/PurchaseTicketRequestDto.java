package com.modelcity.leisure.events.controller.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body for purchasing a ticket (web Checkout flow).
 *
 * <p>Extensible DTO (plain class, not a {@code record}): a city may subclass it to accept extra input fields
 * and bind the subtype through {@code PurchaseTicketUseCase<T extends TicketDto, R extends PurchaseTicketRequestDto>}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseTicketRequestDto {

    private String checkoutSessionId;
}
