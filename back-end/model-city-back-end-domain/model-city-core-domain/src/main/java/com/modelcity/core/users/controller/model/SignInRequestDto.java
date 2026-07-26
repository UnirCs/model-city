package com.modelcity.core.users.controller.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body for the {@code POST /sign-ins} endpoint.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignInRequestDto {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(max = 100)
    private String neighbourhoodName;
}

