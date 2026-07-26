package com.modelcity.mobility.cars.controller.model;

import com.modelcity.mobility.cars.store.model.CarView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Full representation of a citizen's car.
 *
 * <p>Extensible DTO (plain class, not a {@code record}): a local deployment may subclass it to add
 * city-specific fields and have its overridden use cases / controllers work with the subtype through the
 * generic seams (e.g. {@code GetUserCarsUseCase<T extends CarDto>}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarDto {

    private Long id;
    private String ownerSub;
    private String licensePlate;
    private String nickname;
    private String brand;
    private String model;
    private OffsetDateTime createdAt;

    public static CarDto from(CarView c) {
        return new CarDto(c.getId(), c.getOwnerSub(), c.getLicensePlate(),
                c.getNickname(), c.getBrand(), c.getModel(), c.getCreatedAt());
    }
}
