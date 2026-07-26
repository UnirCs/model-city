package com.modelcity.engagement.questions.store;

import com.modelcity.engagement.questions.controller.model.CivicQuestionRequestDto;
import com.modelcity.engagement.questions.repository.CivicQuestionRepository;
import com.modelcity.engagement.questions.repository.CivicQuestionSpecs;
import com.modelcity.engagement.questions.repository.model.CivicQuestion;
import com.modelcity.engagement.questions.repository.model.Objective;
import com.modelcity.engagement.questions.store.model.CivicQuestionView;
import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.common.i18n.LocalizedText;
import com.modelcity.core.users.repository.NeighbourhoodRepository;
import com.modelcity.core.users.repository.ZoneRepository;
import com.modelcity.core.users.repository.model.Neighbourhood;
import com.modelcity.core.users.repository.model.Zone;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Monolith JPA adapter for the civic-question persistence port: resolves and links the real
 * Zone/Neighbourhood entities (single shared database); the default {@link CivicQuestionStore} bean.
 */
@Component
@ModelCityDisabledIfInherited
@RequiredArgsConstructor
public class DefaultCivicQuestionStore implements CivicQuestionStore<CivicQuestion, CivicQuestionRequestDto> {

    private final CivicQuestionRepository<CivicQuestion> civicQuestionRepository;
    private final ZoneRepository zoneRepository;
    private final NeighbourhoodRepository neighbourhoodRepository;

    @Override
    public Optional<CivicQuestion> findById(Long id) {
        return civicQuestionRepository.findById(id);
    }

    @Override
    public Page<CivicQuestion> search(String status, Long zoneId, Long neighbourhoodId, Pageable pageable) {
        Specification<CivicQuestion> spec = statusSpec(status, LocalDate.now());
        if (zoneId != null) {
            spec = spec.and(CivicQuestionSpecs.withZone(zoneId));
        }
        if (neighbourhoodId != null) {
            spec = spec.and(CivicQuestionSpecs.withNeighbourhood(neighbourhoodId));
        }
        return civicQuestionRepository.findAll(spec, pageable);
    }

    @Override
    public CivicQuestion create(CivicQuestionRequestDto request) {
        CivicQuestion question = CivicQuestion.builder()
                .objectives(new ArrayList<>())
                .answers(new ArrayList<>())
                .build();
        applyFields(question, request);
        return civicQuestionRepository.save(question);
    }

    @Override
    public CivicQuestion update(Long id, CivicQuestionRequestDto request) {
        CivicQuestion question = civicQuestionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
        applyFields(question, request);
        return civicQuestionRepository.save(question);
    }

    private Specification<CivicQuestion> statusSpec(String status, LocalDate today) {
        return switch (status == null ? "" : status.toLowerCase()) {
            case "active" -> CivicQuestionSpecs.active(today);
            case "past"   -> CivicQuestionSpecs.past(today);
            case "future" -> CivicQuestionSpecs.future(today);
            default        -> (root, query, cb) -> null;
        };
    }

    private void applyFields(CivicQuestion question, CivicQuestionRequestDto request) {
        question.setTitle(LocalizedText.requireDefault("title", request.getTitle()));
        question.setDescription(LocalizedText.requireDefault("description", request.getDescription()));
        question.setImageUrl(request.getImageUrl());
        question.setOpenDate(request.getOpenDate());
        question.setCloseDate(request.getCloseDate());
        question.setZone(resolveZone(request.getZoneId()));
        question.setNeighbourhood(resolveNeighbourhood(request.getNeighbourhoodId()));
        applyTranslations(question, request);
        applyObjectives(question, request);
    }

    private void applyTranslations(CivicQuestion question, CivicQuestionRequestDto request) {
        Map<String, String> title = LocalizedText.nonDefault(request.getTitle());
        Map<String, String> description = LocalizedText.nonDefault(request.getDescription());
        Set<String> locales = new HashSet<>();
        locales.addAll(title.keySet());
        locales.addAll(description.keySet());
        Map<String, CivicQuestion.CivicQuestionI18n> translations = new HashMap<>();
        for (String locale : locales) {
            translations.put(locale, new CivicQuestion.CivicQuestionI18n(title.get(locale), description.get(locale)));
        }
        question.getTranslations().clear();
        question.getTranslations().putAll(translations);
    }

    private void applyObjectives(CivicQuestion question, CivicQuestionRequestDto request) {
        question.getObjectives().clear();
        if (request.getObjectives() == null) return;
        List<Objective> objectives = new ArrayList<>();
        for (var o : request.getObjectives()) {
            Objective obj = new Objective();
            obj.setQuestion(question);
            obj.setObjective(LocalizedText.requireDefault("objective", o.getObjective()));
            obj.setSortOrder(o.getSortOrder());
            Map<String, String> objectiveTr = LocalizedText.nonDefault(o.getObjective());
            Map<String, Objective.ObjectiveI18n> trMap = new HashMap<>();
            objectiveTr.forEach((locale, value) -> trMap.put(locale, new Objective.ObjectiveI18n(value)));
            obj.getTranslations().clear();
            obj.getTranslations().putAll(trMap);
            objectives.add(obj);
        }
        question.getObjectives().addAll(objectives);
    }

    private Zone resolveZone(Long zoneId) {
        return zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", zoneId));
    }

    private Neighbourhood resolveNeighbourhood(Long neighbourhoodId) {
        return neighbourhoodRepository.findById(neighbourhoodId)
                .orElseThrow(() -> new ResourceNotFoundException("Neighbourhood", neighbourhoodId));
    }
}
