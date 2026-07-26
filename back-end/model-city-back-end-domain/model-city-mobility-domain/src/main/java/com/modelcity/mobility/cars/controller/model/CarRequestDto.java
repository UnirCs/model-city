package com.modelcity.mobility.cars.controller.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Body to create a new car for a citizen.
 *
 * <p>Extensible DTO (plain class, not a {@code record}): a city may subclass it to accept extra input
 * fields and bind the subtype through the generic seams
 * (e.g. {@code CreateCarUseCase<T extends CarDto, R extends CarRequestDto>}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarRequestDto {

    @NotBlank @Size(max = 32)
    private String licensePlate;
    @Size(max = 128)
    private String nickname;
    @Size(max = 128)
    private String brand;
    @Size(max = 128)
    private String model;
}
