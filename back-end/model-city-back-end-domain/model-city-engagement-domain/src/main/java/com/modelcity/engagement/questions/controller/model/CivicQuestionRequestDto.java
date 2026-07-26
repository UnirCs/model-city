package com.modelcity.engagement.questions.controller.model;

import com.modelcity.engagement.questions.store.model.CivicQuestionView;
import com.modelcity.engagement.questions.store.model.ObjectiveView;
import com.modelcity.common.i18n.SupportedLocale;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Request body for POST and PUT on civic questions. The localizable {@code title} and
 * {@code description} are multi-locale maps ({@code locale -> text}) with a mandatory {@code es} entry.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CivicQuestionRequestDto {

    @NotEmpty private Map<String, String> title;
    @NotEmpty private Map<String, String> description;
    private String imageUrl;
    @NotNull private LocalDate openDate;
    @NotNull private LocalDate closeDate;
    @NotNull private Long zoneId;
    @NotNull private Long neighbourhoodId;
    @Valid private List<ObjectiveRequestDto> objectives;

    public static CivicQuestionRequestDto fromEntity(CivicQuestionView q) {
        List<ObjectiveRequestDto> objectives = q.getObjectives().stream()
                .map(o -> new ObjectiveRequestDto(objectiveLocaleMap(o), o.getSortOrder()))
                .toList();
        return new CivicQuestionRequestDto(
                localeMap(q, q.getTitle(), CivicQuestionView.Translation::getTitle),
                localeMap(q, q.getDescription(), CivicQuestionView.Translation::getDescription),
                q.getImageUrl(),
                q.getOpenDate(), q.getCloseDate(),
                q.getZoneId(), q.getNeighbourhoodId(),
                objectives
        );
    }

    private static Map<String, String> localeMap(CivicQuestionView q, String base,
                                                 Function<CivicQuestionView.Translation, String> field) {
        Map<String, String> values = new LinkedHashMap<>();
        if (base != null) values.put(SupportedLocale.DEFAULT.code(), base);
        q.getTranslations().forEach((code, t) -> {
            String value = field.apply(t);
            if (value != null && !value.isBlank()) values.put(code, value);
        });
        return values;
    }

    private static Map<String, String> objectiveLocaleMap(ObjectiveView o) {
        Map<String, String> values = new LinkedHashMap<>();
        if (o.getObjective() != null) values.put(SupportedLocale.DEFAULT.code(), o.getObjective());
        o.getTranslations().forEach((code, t) -> {
            if (t.getObjective() != null && !t.getObjective().isBlank()) values.put(code, t.getObjective());
        });
        return values;
    }
}
