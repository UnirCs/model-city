package com.modelcity.engagement.questions.controller.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Response returned after a successful answer submission. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResponseDto {

    private Long answerId;
}
