package com.modelcity.leisure.publicspaces.controller.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Request body for creating or replacing a public space (admin only). Localizable {@code name},
 * {@code description} and {@code address} are multi-locale maps ({@code locale -> text}); the
 * {@code es} entry of {@code name} and {@code description} is mandatory.
 *
 * <p>Extensible DTO (plain class, not a {@code record}): a city may subclass it and bind the subtype through
 * the generic seams (e.g. {@code CreatePublicSpaceUseCase<T extends PublicSpaceDto, R extends PublicSpaceRequestDto>}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PublicSpaceRequestDto {

    @NotEmpty
    private Map<String, String> name;
    @NotEmpty
    private Map<String, String> description;
    private Map<String, String> address;
    @DecimalMin("-90")
    @DecimalMax("90")
    private Double latitude;
    @DecimalMin("-180")
    @DecimalMax("180")
    private Double longitude;
    @Size(max = 3)
    private List<String> photoUrls;
}
