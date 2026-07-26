package com.modelcity.common.trails;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SystemTrailSpecificationsTest {

    @Mock Root<Object> root;
    @Mock CriteriaQuery<?> query;
    @Mock CriteriaBuilder cb;
    @Mock Path path;
    @Mock Predicate conjunction;
    @Mock Predicate combined;

    @BeforeEach
    void setUp() {
        when(root.get(anyString())).thenReturn(path);
        when(cb.conjunction()).thenReturn(conjunction);
        when(cb.and(any(), any())).thenReturn(combined);
        when(cb.equal(any(Expression.class), any(Object.class))).thenReturn(combined);
        when(cb.greaterThanOrEqualTo(any(Expression.class), any(Comparable.class))).thenReturn(combined);
        when(cb.lessThanOrEqualTo(any(Expression.class), any(Comparable.class))).thenReturn(combined);
    }

    @Test
    void matching_emptyQuery_returnsBareConjunction() {
        Specification<Object> spec = SystemTrailSpecifications.matching(new SystemTrailQuery(null, null, null, null));

        Predicate result = spec.toPredicate(root, query, cb);

        assertThat(result).isSameAs(conjunction);
        verify(cb, never()).and(any(), any());
    }

    @Test
    void matching_eventTypeOnly_addsEqualPredicate() {
        Specification<Object> spec = SystemTrailSpecifications.matching(
                new SystemTrailQuery("CITY_PLACE_CREATED", null, null, null));

        spec.toPredicate(root, query, cb);

        verify(cb).equal(path, "CITY_PLACE_CREATED");
        verify(cb).and(conjunction, combined);
    }

    @Test
    void matching_blankEventType_isIgnored() {
        Specification<Object> spec = SystemTrailSpecifications.matching(
                new SystemTrailQuery("   ", null, null, null));

        Predicate result = spec.toPredicate(root, query, cb);

        assertThat(result).isSameAs(conjunction);
        verify(cb, never()).equal(any(), any());
    }

    @Test
    void matching_responsibleUserId_addsEqualPredicate() {
        Specification<Object> spec = SystemTrailSpecifications.matching(
                new SystemTrailQuery(null, "agent-sub", null, null));

        spec.toPredicate(root, query, cb);

        verify(cb).equal(path, "agent-sub");
    }

    @Test
    void matching_fromAndTo_addsRangePredicates() {
        OffsetDateTime from = OffsetDateTime.now().minusDays(1);
        OffsetDateTime to = OffsetDateTime.now();
        Specification<Object> spec = SystemTrailSpecifications.matching(new SystemTrailQuery(null, null, from, to));

        spec.toPredicate(root, query, cb);

        verify(cb).greaterThanOrEqualTo(path, from);
        verify(cb).lessThanOrEqualTo(path, to);
    }

    @Test
    void matching_allFieldsPresent_combinesAllPredicates() {
        OffsetDateTime from = OffsetDateTime.now().minusDays(1);
        OffsetDateTime to = OffsetDateTime.now();
        Specification<Object> spec = SystemTrailSpecifications.matching(
                new SystemTrailQuery("CITY_PLACE_CREATED", "agent-sub", from, to));

        spec.toPredicate(root, query, cb);

        verify(cb, times(4)).and(any(), any());
    }
}
