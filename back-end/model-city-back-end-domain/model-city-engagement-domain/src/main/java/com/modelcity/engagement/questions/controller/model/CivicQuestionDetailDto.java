package com.modelcity.engagement.questions.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.modelcity.engagement.questions.store.model.CivicQuestionView;
import com.modelcity.common.i18n.LocalizedText;
import com.modelcity.common.i18n.SupportedLocale;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Full representation of a civic question for detail endpoints, resolved to the requested locale. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CivicQuestionDetailDto {

    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private LocalDate openDate;
    private LocalDate closeDate;
    private Long zoneId;
    private Long neighbourhoodId;
    private List<ObjectiveDto> objectives;
    private long yesVotes;
    private long noVotes;
    private boolean submittable;
    private Map<String, Map<String, String>> translations;

    public static CivicQuestionDetailDto from(CivicQuestionView q, long yesVotes, long noVotes, String locale) {
        return build(q, yesVotes, noVotes, locale, false);
    }

    public static CivicQuestionDetailDto fromWithTranslations(CivicQuestionView q, long yesVotes, long noVotes, String locale) {
        return build(q, yesVotes, noVotes, locale, true);
    }

    private static CivicQuestionDetailDto build(CivicQuestionView q, long yesVotes, long noVotes,
                                                String locale, boolean includeTranslations) {
        CivicQuestionView.Translation t = q.getTranslations().get(locale);
        List<ObjectiveDto> objectives = q.getObjectives().stream()
                .map(o -> includeTranslations ? ObjectiveDto.fromWithTranslations(o, locale) : ObjectiveDto.from(o, locale))
                .toList();
        return new CivicQuestionDetailDto(
                q.getId(),
                LocalizedText.resolve(q.getTitle(), t == null ? null : t.getTitle()),
                LocalizedText.resolve(q.getDescription(), t == null ? null : t.getDescription()),
                q.getImageUrl(),
                q.getOpenDate(), q.getCloseDate(),
                q.getZoneId(), q.getNeighbourhoodId(),
                objectives, yesVotes, noVotes, false,
                includeTranslations ? questionTranslations(q) : null
        );
    }

    /** Returns a copy with the per-user {@code submittable} flag resolved outside the shared cache. */
    public CivicQuestionDetailDto withSubmittable(boolean submittable) {
        return new CivicQuestionDetailDto(id, title, description, imageUrl, openDate, closeDate,
                zoneId, neighbourhoodId, objectives, yesVotes, noVotes, submittable, translations);
    }

    private static Map<String, Map<String, String>> questionTranslations(CivicQuestionView q) {
        Map<String, Map<String, String>> all = new LinkedHashMap<>();
        all.put("title", localeMap(q.getTitle(), CivicQuestionView.Translation::getTitle, q));
        all.put("description", localeMap(q.getDescription(), CivicQuestionView.Translation::getDescription, q));
        return all;
    }

    private static Map<String, String> localeMap(String base,
                                                 Function<CivicQuestionView.Translation, String> field,
                                                 CivicQuestionView q) {
        Map<String, String> values = new LinkedHashMap<>();
        if (base != null) values.put(SupportedLocale.DEFAULT.code(), base);
        q.getTranslations().forEach((code, t) -> {
            String value = field.apply(t);
            if (value != null && !value.isBlank()) values.put(code, value);
        });
        return values;
    }
}
