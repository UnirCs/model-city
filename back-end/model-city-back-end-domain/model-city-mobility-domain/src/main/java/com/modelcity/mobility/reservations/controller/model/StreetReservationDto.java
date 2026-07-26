package com.modelcity.mobility.reservations.controller.model;

import com.modelcity.mobility.reservations.store.model.StreetReservationView;
import com.modelcity.mobility.reservations.repository.model.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Full representation of a street reservation.
 *
 * <p>Extensible DTO (plain class, not a {@code record}): a local deployment may subclass it to add
 * city-specific fields and have its overridden use cases / controllers work with the subtype through the
 * generic seams (e.g. {@code GetStreetReservationsUseCase<T extends StreetReservationDto>}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StreetReservationDto {

    private Long id;
    private String userSub;
    private Long carId;
    private String licensePlate;
    private String carNickname;
    private Double latitude;
    private Double longitude;
    private OffsetDateTime createdAt;
    private OffsetDateTime expiresAt;
    private Long renewedFromId;
    private boolean active;
    private ReservationStatus status;
    private BigDecimal pricePaid;
    private String currency;
    private String stripeCheckoutSessionId;

    public static StreetReservationDto from(StreetReservationView r) {
        return new StreetReservationDto(
                r.getId(),
                r.getUserSub(),
                r.getCar() != null ? r.getCar().getId() : null,
                r.getCar() != null ? r.getCar().getLicensePlate() : null,
                r.getCar() != null ? r.getCar().getNickname() : null,
                r.getLatitude(),
                r.getLongitude(),
                r.getCreatedAt(),
                r.getExpiresAt(),
                r.getRenewedFromId(),
                r.getExpiresAt() != null && r.getExpiresAt().isAfter(OffsetDateTime.now()),
                r.getStatus(),
                r.getPricePaid(),
                r.getCurrency(),
                r.getStripeCheckoutSessionId()
        );
    }
}
