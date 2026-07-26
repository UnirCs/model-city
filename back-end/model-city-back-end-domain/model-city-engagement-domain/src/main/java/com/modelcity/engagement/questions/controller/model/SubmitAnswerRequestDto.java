package com.modelcity.engagement.questions.controller.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAnswerRequestDto {

    @NotNull private UUID operationAuthorizationId;
    @NotBlank @Pattern(regexp = "YES|NO", message = "vote must be YES or NO") private String vote;
}
