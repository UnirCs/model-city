package com.modelcity.engagement.questions.store;

import com.modelcity.engagement.questions.controller.model.CivicQuestionRequestDto;
import com.modelcity.engagement.questions.store.model.CivicQuestionView;
import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Persistence port for civic questions.
 *
 * <p>Generic over the read type {@code T extends CivicQuestionView} and the write type
 * {@code R extends CivicQuestionRequestDto} (like the use cases / controllers), so a city can bind its own
 * richer view/request without casting. The platform default binds {@code T} to the concrete
 * {@code CivicQuestion} entity (the microservice stores soft references zoneId/neighbourhoodId, the
 * monolith resolves and links the real Zone/Neighbourhood entities); consumers inject
 * {@code CivicQuestionStore<? extends CivicQuestionView, CivicQuestionRequestDto>}.
 */
@ModelCityExtensionPoint
public interface CivicQuestionStore<T extends CivicQuestionView, R extends CivicQuestionRequestDto> {

    Optional<T> findById(Long id);

    /** Paginated search filtered by status ("active"/"past"/"future"/other) and optional zone/neighbourhood. */
    Page<T> search(String status, Long zoneId, Long neighbourhoodId, Pageable pageable);

    /** Builds and persists a new civic question from the request (including objectives). */
    T create(R request);

    /** Applies the request onto the existing question (id assumed to exist) and persists it. */
    T update(Long id, R request);
}
