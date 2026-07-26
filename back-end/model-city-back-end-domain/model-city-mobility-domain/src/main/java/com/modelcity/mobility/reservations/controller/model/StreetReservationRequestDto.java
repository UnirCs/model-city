package com.modelcity.mobility.reservations.controller.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Body to create or renew a street reservation. Duration in minutes (20..240).
 *
 * <p>Extensible DTO (plain class, not a {@code record}): a city may subclass it and bind the subtype through
 * the generic seams (e.g. {@code CreateStreetReservationUseCase<T extends StreetReservationDto, R extends StreetReservationRequestDto>}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StreetReservationRequestDto {

    @NotNull
    private Long carId;
    @NotNull @DecimalMin("-90") @DecimalMax("90")
    private Double latitude;
    @NotNull @DecimalMin("-180") @DecimalMax("180")
    private Double longitude;
    @NotNull @Min(20) @Max(240)
    private Integer durationMinutes;
    @NotBlank
    private String checkoutSessionId;
    @NotNull @DecimalMin("0")
    private BigDecimal price;
}
