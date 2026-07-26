package com.modelcity.leisure.publicspaces.controller.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Request body for creating or replacing a reservable resource. Localizable {@code name} and
 * {@code description} are multi-locale maps ({@code locale -> text}); the {@code es} entry of
 * {@code name} is mandatory.
 *
 * <p>Extensible DTO (plain class, not a {@code record}): a city may subclass it and bind the subtype through
 * the generic seams (e.g. {@code CreateReservableResourceUseCase<T extends ReservableResourceDto, R extends ReservableResourceRequestDto>}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservableResourceRequestDto {

    @NotEmpty
    private Map<String, String> name;
    private Map<String, String> description;
    @NotBlank
    private String resourceType;
}
