package com.modelcity.mobility.sanctions.controller.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Body to issue a new sanction.
 *
 * <p>Extensible DTO (plain class, not a {@code record}): a city may subclass it and bind the subtype through
 * the generic seams (e.g. {@code CreateSanctionUseCase<T extends SanctionDto, R extends SanctionRequestDto>}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SanctionRequestDto {

    @NotBlank @Size(max = 32)
    private String licensePlate;
    @NotNull @DecimalMin("-90") @DecimalMax("90")
    private Double latitude;
    @NotNull @DecimalMin("-180") @DecimalMax("180")
    private Double longitude;
    @NotBlank
    private String imageBase64;
}
