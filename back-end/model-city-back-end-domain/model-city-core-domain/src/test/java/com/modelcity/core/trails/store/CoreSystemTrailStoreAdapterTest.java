package com.modelcity.core.trails.store;

import com.modelcity.common.trails.NewSystemTrail;
import com.modelcity.common.trails.OperationType;
import com.modelcity.common.trails.SystemTrailQuery;
import com.modelcity.core.trails.repository.CoreTrailRepository;
import com.modelcity.core.trails.repository.model.CoreTrail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoreSystemTrailStoreAdapterTest {

    @Mock
    CoreTrailRepository repository;

    CoreSystemTrailStoreAdapter store;

    @BeforeEach
    void setUp() {
        store = new CoreSystemTrailStoreAdapter(repository);
    }

    @Test
    void save_buildsEntityFromCommandAndPersists() {
        UUID eventId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        NewSystemTrail command = new NewSystemTrail(eventId, "USER_REGISTERED", OperationType.CREATE, now,
                "corr-1", "user-sub", "MODEL-CITY-CITIZEN", 5L, 1L, "USER", "user-sub", "{}");

        when(repository.save(any(CoreTrail.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = store.save(command);

        assertThat(result.getEventId()).isEqualTo(eventId);
        assertThat(result.getEventType()).isEqualTo("USER_REGISTERED");
        assertThat(result.getOperationType()).isEqualTo(OperationType.CREATE);
        assertThat(result.getCorrelationId()).isEqualTo("corr-1");
        assertThat(result.getResponsibleUserId()).isEqualTo("user-sub");
        assertThat(result.getNeighbourhoodId()).isEqualTo(5L);
        assertThat(result.getZoneId()).isEqualTo(1L);
        assertThat(result.getResourceType()).isEqualTo("USER");
        assertThat(result.getResourceId()).isEqualTo("user-sub");
        assertThat(result.getPayload()).isEqualTo("{}");
    }

    @Test
    @SuppressWarnings("unchecked")
    void search_delegatesToRepositoryWithSpecification() {
        CoreTrail trail = CoreTrail.builder().eventId(UUID.randomUUID()).build();
        Page<CoreTrail> page = new PageImpl<>(List.of(trail));
        Pageable pageable = PageRequest.of(0, 20);
        doReturn(page).when(repository).findAll(any(Specification.class), eq(pageable));

        SystemTrailQuery query = new SystemTrailQuery(null, null, null, null);
        var result = store.search(query, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }
}
