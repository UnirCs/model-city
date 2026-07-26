package com.modelcity.core.users.controller.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request body for POST /agents. Role must be BACKOFFICE, OPERATOR or MOBILITY_AGENT. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InviteAgentRequestDto {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Pattern(
        regexp = "MODEL-CITY-BACKOFFICE|MODEL-CITY-OPERATOR|MODEL-CITY-MOBILITY-AGENT",
        message = "role must be MODEL-CITY-BACKOFFICE, MODEL-CITY-OPERATOR or MODEL-CITY-MOBILITY-AGENT"
    )
    private String role;
}

