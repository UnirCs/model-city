package com.modelcity.engagement.questions.controller.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * One objective entry in a create/update civic question request. The localizable {@code objective}
 * text is a multi-locale map ({@code locale -> text}) with a mandatory {@code es} entry.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ObjectiveRequestDto {

    @NotEmpty private Map<String, String> objective;
    private int sortOrder;
}
