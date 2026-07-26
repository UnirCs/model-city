package com.modelcity.core.otp.controller.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidateChallengeRequestDto {

    @NotBlank private String otp;
    @NotBlank private String operationType;
    @NotBlank private String resourceType;
    @NotBlank private String resourceId;
}

