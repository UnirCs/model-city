package com.modelcity.engagement.questions.controller.model;

import com.modelcity.engagement.questions.store.model.CivicQuestionView;
import com.modelcity.common.i18n.LocalizedText;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Partial representation of a civic question for list endpoints, with its title resolved to the requested locale. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CivicQuestionSummaryDto {

    private Long id;
    private String title;
    private String imageUrl;
    private LocalDate openDate;
    private LocalDate closeDate;
    private Long zoneId;
    private Long neighbourhoodId;

    public static CivicQuestionSummaryDto from(CivicQuestionView q, String locale) {
        CivicQuestionView.Translation t = q.getTranslations().get(locale);
        return new CivicQuestionSummaryDto(
                q.getId(),
                LocalizedText.resolve(q.getTitle(), t == null ? null : t.getTitle()),
                q.getImageUrl(),
                q.getOpenDate(), q.getCloseDate(),
                q.getZoneId(), q.getNeighbourhoodId()
        );
    }
}
