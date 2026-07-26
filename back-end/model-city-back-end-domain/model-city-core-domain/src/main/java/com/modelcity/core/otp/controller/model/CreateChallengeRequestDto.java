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
public class CreateChallengeRequestDto {

    @NotBlank private String operationType;
    @NotBlank private String resourceType;
    @NotBlank private String resourceId;

    /**
     * Core-signed token issued by {@code /certificate-verifications}, carrying the verified
     * {@code dni_hash}. Required for operations that must be tied to a verified DNI (e.g. voting);
     * optional otherwise.
     */
    private String verificationToken;
}

