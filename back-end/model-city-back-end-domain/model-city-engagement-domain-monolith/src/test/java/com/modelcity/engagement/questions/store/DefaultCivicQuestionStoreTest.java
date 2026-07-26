package com.modelcity.engagement.questions.store;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.core.users.repository.NeighbourhoodRepository;
import com.modelcity.core.users.repository.ZoneRepository;
import com.modelcity.core.users.repository.model.Neighbourhood;
import com.modelcity.core.users.repository.model.Zone;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DefaultCivicQuestionStoreTest {

    @Mock
    CivicQuestionRepository<CivicQuestion> civicQuestionRepository;

    @Mock
    ZoneRepository zoneRepository;

    @Mock
    NeighbourhoodRepository neighbourhoodRepository;

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
        store = new DefaultCivicQuestionStore(civicQuestionRepository, zoneRepository, neighbourhoodRepository);
        when(root.get(anyString())).thenReturn((Path) path);
        when(cb.and(any(), any())).thenReturn(predicate);
        when(cb.lessThanOrEqualTo(any(), any(Comparable.class))).thenReturn(predicate);
        when(cb.greaterThanOrEqualTo(any(), any(Comparable.class))).thenReturn(predicate);
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

    @Test
    void create_resolvesZoneAndNeighbourhoodAndAppliesFields() {
        Zone zone = new Zone();
        zone.setId(1L);
        Neighbourhood neighbourhood = new Neighbourhood();
        neighbourhood.setId(5L);

        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(neighbourhoodRepository.findById(5L)).thenReturn(Optional.of(neighbourhood));
        when(civicQuestionRepository.save(any(CivicQuestion.class))).thenAnswer(inv -> inv.getArgument(0));

        CivicQuestion result = store.create(buildRequest());

        assertThat(result.getZoneId()).isEqualTo(1L);
        assertThat(result.getNeighbourhoodId()).isEqualTo(5L);
        assertThat(result.getTitle()).isEqualTo("Pregunta");
        assertThat(result.getObjectives()).hasSize(1);
    }

    @Test
    void create_zoneNotFound_throwsResourceNotFound() {
        when(zoneRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> store.create(buildRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_neighbourhoodNotFound_throwsResourceNotFound() {
        Zone zone = new Zone();
        zone.setId(1L);
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(neighbourhoodRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> store.create(buildRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_notFound_throwsNotFound() {
        when(civicQuestionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> store.update(99L, buildRequest()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Question not found");
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
    void search_activeStatus_buildsAndPredicate() {
        Specification<CivicQuestion> spec = captureSearchSpec("active");

        Predicate result = spec.toPredicate(root, query, cb);

        assertThat(result).isSameAs(predicate);
        verify(cb).and(predicate, predicate);
    }

    @Test
    void search_unknownStatus_returnsNullPredicate() {
        Specification<CivicQuestion> spec = captureSearchSpec("bogus");

        Predicate result = spec.toPredicate(root, query, cb);

        assertThat(result).isNull();
        verifyNoInteractions(cb);
    }

    @Test
    void findById_delegatesToRepository() {
        store.findById(1L);
        verify(civicQuestionRepository).findById(1L);
    }
}
