package com.modelcity.engagement.trails;

import com.modelcity.engagement.alerts.store.model.SecurityAlertView;
import com.modelcity.engagement.trails.store.EngagementSystemTrailStore;
import com.modelcity.engagement.questions.store.model.CivicQuestionView;
import com.modelcity.common.trails.AbstractSystemTrailGenerator;
import com.modelcity.common.trails.OperationType;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

/** Records engagement system trails (civic questions, answers, security alerts). */
@Service("citizenEngagementSystemTrailGenerator")
public class SystemTrailGenerator extends AbstractSystemTrailGenerator {

    private static final String CITIZEN_ROLE = "MODEL-CITY-CITIZEN";
    private static final String RES_CIVIC_QUESTION = "CIVIC_QUESTION";
    private static final String RES_CIVIC_ANSWER = "CIVIC_ANSWER";
    private static final String RES_SECURITY_ALERT = "SECURITY_ALERT";

    public SystemTrailGenerator(EngagementSystemTrailStore store, ObjectMapper objectMapper) {
        super(store, objectMapper);
    }

    public void civicQuestionCreated(String responsibleUserId, CivicQuestionView q) {
        record("CIVIC_QUESTION_CREATED", OperationType.CREATE, responsibleUserId, null,
                q.getNeighbourhoodId(), q.getZoneId(), RES_CIVIC_QUESTION, String.valueOf(q.getId()),
                questionPayload(q, null));
    }

    public void civicQuestionUpdated(String responsibleUserId, CivicQuestionView q, String updateType) {
        record("CIVIC_QUESTION_UPDATED", OperationType.UPDATE, responsibleUserId, null,
                q.getNeighbourhoodId(), q.getZoneId(), RES_CIVIC_QUESTION, String.valueOf(q.getId()),
                questionPayload(q, updateType));
    }

    public void answerSubmitted(String citizenSub, Long answerId, CivicQuestionView q, String vote) {
        record("ANSWER_SUBMITTED", OperationType.CREATE, citizenSub, CITIZEN_ROLE,
                q.getNeighbourhoodId(), q.getZoneId(), RES_CIVIC_ANSWER, String.valueOf(answerId),
                payload("answerId", answerId, "questionId", q.getId(), "citizenSub", citizenSub,
                        "vote", vote, "questionNeighbourhoodId", q.getNeighbourhoodId(),
                        "questionZoneId", q.getZoneId()));
    }

    public void securityAlertCreated(String responsibleUserId, SecurityAlertView a) {
        record("SECURITY_ALERT_CREATED", OperationType.CREATE, responsibleUserId, null,
                a.getNeighbourhoodId(), a.getZoneId(), RES_SECURITY_ALERT, String.valueOf(a.getId()),
                alertPayload(a));
    }

    public void securityAlertDeleted(String responsibleUserId, SecurityAlertView a) {
        record("SECURITY_ALERT_DELETED", OperationType.DELETE, responsibleUserId, null,
                a.getNeighbourhoodId(), a.getZoneId(), RES_SECURITY_ALERT, String.valueOf(a.getId()),
                alertPayload(a));
    }

    private static Object questionPayload(CivicQuestionView q, String updateType) {
        return payload(
                "questionId", q.getId(),
                "title", q.getTitle(),
                "openDate", q.getOpenDate() == null ? null : q.getOpenDate().toString(),
                "closeDate", q.getCloseDate() == null ? null : q.getCloseDate().toString(),
                "zoneId", q.getZoneId(),
                "neighbourhoodId", q.getNeighbourhoodId(),
                "objectiveCount", q.getObjectives() == null ? 0 : q.getObjectives().size(),
                "updateType", updateType);
    }

    private static Object alertPayload(SecurityAlertView a) {
        return payload(
                "alertId", a.getId(),
                "title", a.getTitle(),
                "severity", a.getSeverity() == null ? null : a.getSeverity().name(),
                "latitude", a.getLatitude(),
                "longitude", a.getLongitude(),
                "zoneId", a.getZoneId(),
                "neighbourhoodId", a.getNeighbourhoodId(),
                "expiresAt", a.getExpiresAt() == null ? null : a.getExpiresAt().toString());
    }
}
