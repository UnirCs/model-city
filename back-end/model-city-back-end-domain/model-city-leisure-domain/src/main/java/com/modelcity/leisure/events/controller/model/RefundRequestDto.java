package com.modelcity.leisure.events.controller.model;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Optional body for a manual refund issued from the backoffice.
 *
 * <p>Extensible DTO (plain class, not a {@code record}): a city may subclass it to accept extra input fields
 * and bind the subtype through {@code RefundTicketUseCase<T extends RefundDto, R extends RefundRequestDto>}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequestDto {

    @Size(max = 512)
    private String reason;
}
