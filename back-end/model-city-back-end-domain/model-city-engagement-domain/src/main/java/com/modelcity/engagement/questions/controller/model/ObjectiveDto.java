package com.modelcity.engagement.questions.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.modelcity.engagement.questions.store.model.ObjectiveView;
import com.modelcity.common.i18n.LocalizedText;
import com.modelcity.common.i18n.SupportedLocale;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/** DTO representing a single objective of a civic question, resolved to the requested locale. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ObjectiveDto {

    private Long id;
    private String objective;
    private int sortOrder;
    private Map<String, String> translations;

    public static ObjectiveDto from(ObjectiveView o, String locale) {
        ObjectiveView.Translation t = o.getTranslations().get(locale);
        return new ObjectiveDto(o.getId(),
                LocalizedText.resolve(o.getObjective(), t == null ? null : t.getObjective()),
                o.getSortOrder(), null);
    }

    public static ObjectiveDto fromWithTranslations(ObjectiveView o, String locale) {
        ObjectiveView.Translation t = o.getTranslations().get(locale);
        Map<String, String> all = new LinkedHashMap<>();
        if (o.getObjective() != null) all.put(SupportedLocale.DEFAULT.code(), o.getObjective());
        o.getTranslations().forEach((code, tr) -> {
            if (tr.getObjective() != null && !tr.getObjective().isBlank()) all.put(code, tr.getObjective());
        });
        return new ObjectiveDto(o.getId(),
                LocalizedText.resolve(o.getObjective(), t == null ? null : t.getObjective()),
                o.getSortOrder(), all);
    }
}
