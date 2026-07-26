package com.modelcity.leisure.events.controller.model;

import com.modelcity.leisure.events.store.model.EventRefundView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Public-facing representation of a refund record.
 *
 * <p>Extensible DTO (plain class, not a {@code record}): a city may subclass it and work with the subtype
 * through the generic seam {@code RefundTicketUseCase<T extends RefundDto, ...>}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefundDto {

    private Long id;
    private Long ticketId;
    private BigDecimal amount;
    private String currency;
    private String reason;
    private boolean automatic;
    private String issuedBySub;
    private LocalDateTime refundedAt;

    public static RefundDto from(EventRefundView r) {
        return new RefundDto(r.getId(), r.getTicketId(), r.getAmount(), r.getCurrency(),
                r.getReason(), r.isAutomatic(), r.getIssuedBySub(), r.getRefundedAt());
    }
}
