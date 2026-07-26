package com.modelcity.engagement.questions.store;

import com.modelcity.engagement.questions.controller.model.CivicQuestionRequestDto;
import com.modelcity.engagement.questions.controller.model.ObjectiveRequestDto;
import com.modelcity.engagement.questions.repository.CivicQuestionRepository;
import com.modelcity.engagement.questions.repository.model.CivicQuestion;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DefaultCivicQuestionStoreTest {

    @Mock
    CivicQuestionRepository<CivicQuestion> civicQuestionRepository;

    @Mock
    Root<CivicQuestion> root;

    @Mock
    CriteriaQuery<?> query;

    @Mock
    CriteriaBuilder cb;

    @Mock
    Path<Object> path;

    @Mock
    Predicate predicate;

    DefaultCivicQuestionStore store;

    @BeforeEach
    void setUp() {
        store = new DefaultCivicQuestionStore(civicQuestionRepository);
        when(root.get(anyString())).thenReturn((Path) path);
        when(cb.and(any(), any())).thenReturn(predicate);
        when(cb.lessThanOrEqualTo(any(), any(Comparable.class))).thenReturn(predicate);
        when(cb.greaterThanOrEqualTo(any(), any(Comparable.class))).thenReturn(predicate);
        when(cb.lessThan(any(), any(Comparable.class))).thenReturn(predicate);
        when(cb.greaterThan(any(), any(Comparable.class))).thenReturn(predicate);
        when(cb.equal(any(), any())).thenReturn(predicate);
    }

    private CivicQuestionRequestDto buildRequest() {
        CivicQuestionRequestDto request = new CivicQuestionRequestDto();
        request.setTitle(Map.of("es", "Pregunta", "en", "Question"));
        request.setDescription(Map.of("es", "Descripción"));
        request.setImageUrl("img.jpg");
        request.setOpenDate(LocalDate.now().plusDays(5));
        request.setCloseDate(LocalDate.now().plusDays(15));
        request.setZoneId(1L);
        request.setNeighbourhoodId(5L);
        ObjectiveRequestDto objective = new ObjectiveRequestDto();
        objective.setObjective(Map.of("es", "Objetivo 1", "en", "Objective 1"));
        objective.setSortOrder(0);
        request.setObjectives(List.of(objective));
        return request;
    }

    @SuppressWarnings("unchecked")
    private Specification<CivicQuestion> captureSearchSpec(String status) {
        Page<CivicQuestion> page = new PageImpl<>(List.of());
        ArgumentCaptor<Specification<CivicQuestion>> captor = ArgumentCaptor.forClass(Specification.class);
        doReturn(page).when(civicQuestionRepository).findAll(captor.capture(), any(Pageable.class));

        store.search(status, null, null, PageRequest.of(0, 3));

        return captor.getValue();
    }

    @Test
    void create_appliesFieldsTranslationsAndObjectives() {
        when(civicQuestionRepository.save(any(CivicQuestion.class))).thenAnswer(inv -> inv.getArgument(0));

        CivicQuestion result = store.create(buildRequest());

        assertThat(result.getTitle()).isEqualTo("Pregunta");
        assertThat(result.getZoneId()).isEqualTo(1L);
        assertThat(result.getTranslations()).containsKey("en");
        assertThat(result.getObjectives()).hasSize(1);
        assertThat(result.getObjectives().get(0).getObjective()).isEqualTo("Objetivo 1");
        assertThat(result.getObjectives().get(0).getTranslations()).containsKey("en");
    }

    @Test
    void update_notFound_throwsNotFound() {
        when(civicQuestionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> store.update(99L, buildRequest()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Question not found");
    }

    @Test
    void update_existingQuestion_replacesObjectives() {
        CivicQuestion existing = CivicQuestion.builder().build();
        when(civicQuestionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(civicQuestionRepository.save(any(CivicQuestion.class))).thenAnswer(inv -> inv.getArgument(0));

        CivicQuestion result = store.update(1L, buildRequest());

        assertThat(result.getObjectives()).hasSize(1);
    }

    @Test
    void search_activeStatus_buildsAndPredicate() {
        Specification<CivicQuestion> spec = captureSearchSpec("active");

        Predicate result = spec.toPredicate(root, query, cb);

        assertThat(result).isSameAs(predicate);
        verify(cb).and(predicate, predicate);
    }

    @Test
    void search_pastStatus_usesLessThanPredicate() {
        Specification<CivicQuestion> spec = captureSearchSpec("past");

        Predicate result = spec.toPredicate(root, query, cb);

        assertThat(result).isSameAs(predicate);
        verify(cb).lessThan(any(), any(Comparable.class));
    }

    @Test
    void search_futureStatus_usesGreaterThanPredicate() {
        Specification<CivicQuestion> spec = captureSearchSpec("future");

        Predicate result = spec.toPredicate(root, query, cb);

        assertThat(result).isSameAs(predicate);
        verify(cb).greaterThan(any(), any(Comparable.class));
    }

    @Test
    void search_unknownStatus_returnsNullPredicate() {
        Specification<CivicQuestion> spec = captureSearchSpec("bogus");

        Predicate result = spec.toPredicate(root, query, cb);

        assertThat(result).isNull();
        verifyNoInteractions(cb);
    }

    @Test
    void search_nullStatus_returnsNullPredicate() {
        Specification<CivicQuestion> spec = captureSearchSpec(null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertThat(result).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void search_withZoneAndNeighbourhood_combinesSpecifications() {
        Page<CivicQuestion> page = new PageImpl<>(List.of());
        doReturn(page).when(civicQuestionRepository).findAll(any(Specification.class), any(Pageable.class));

        store.search("active", 1L, 5L, PageRequest.of(0, 3));

        verify(civicQuestionRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void findById_delegatesToRepository() {
        store.findById(1L);
        verify(civicQuestionRepository).findById(1L);
    }
}
