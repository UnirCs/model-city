package com.modelcity.leisure.trails.store;

import com.modelcity.common.trails.NewSystemTrail;
import com.modelcity.common.trails.OperationType;
import com.modelcity.common.trails.SystemTrailQuery;
import com.modelcity.leisure.trails.repository.LeisureTrailRepository;
import com.modelcity.leisure.trails.repository.model.LeisureTrail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultLeisureSystemTrailStoreTest {

    @Mock
    LeisureTrailRepository<LeisureTrail> repository;

    DefaultLeisureSystemTrailStore store;

    @BeforeEach
    void setUp() {
        store = new DefaultLeisureSystemTrailStore(repository);
    }

    @Test
    void save_buildsEntityFromCommandAndPersists() {
        UUID eventId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        NewSystemTrail command = new NewSystemTrail(eventId, "CITY_PLACE_CREATED", OperationType.CREATE, now,
                "corr-1", "agent-sub", "MODEL-CITY-BACKOFFICE", 5L, 1L, "CITY_PLACE", "10", "{}");

        when(repository.save(any(LeisureTrail.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = store.save(command);

        assertThat(result.getEventId()).isEqualTo(eventId);
        assertThat(result.getEventType()).isEqualTo("CITY_PLACE_CREATED");
        assertThat(result.getOperationType()).isEqualTo(OperationType.CREATE);
        assertThat(result.getResponsibleUserId()).isEqualTo("agent-sub");
        assertThat(result.getNeighbourhoodId()).isEqualTo(5L);
        assertThat(result.getResourceId()).isEqualTo("10");
    }

    @Test
    @SuppressWarnings("unchecked")
    void search_delegatesToRepositoryWithSpecification() {
        LeisureTrail trail = LeisureTrail.builder().eventId(UUID.randomUUID()).build();
        Page<LeisureTrail> page = new PageImpl<>(List.of(trail));
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        doReturn(page).when(repository).findAll(any(Specification.class), eq(pageable));

        SystemTrailQuery query = new SystemTrailQuery(null, null, null, null);
        var result = store.search(query, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }
}
