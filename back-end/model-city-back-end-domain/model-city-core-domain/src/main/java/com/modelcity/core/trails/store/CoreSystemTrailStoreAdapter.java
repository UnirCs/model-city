package com.modelcity.core.trails.store;

import com.modelcity.common.trails.NewSystemTrail;
import com.modelcity.common.trails.SystemTrailQuery;
import com.modelcity.common.trails.SystemTrailSpecifications;
import com.modelcity.common.trails.SystemTrailView;
import com.modelcity.core.trails.repository.CoreTrailRepository;
import com.modelcity.core.trails.repository.model.CoreTrail;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/** JPA adapter for the core system-trail port. The {@code CoreTrail} entity is shared by both topologies. */
@Component
@RequiredArgsConstructor
public class CoreSystemTrailStoreAdapter implements CoreSystemTrailStore {

    private final CoreTrailRepository repository;

    @Override
    public SystemTrailView save(NewSystemTrail e) {
        CoreTrail entity = CoreTrail.builder()
                .eventId(e.eventId())
                .eventType(e.eventType())
                .operationType(e.operationType())
                .occurredAt(e.occurredAt())
                .correlationId(e.correlationId())
                .responsibleUserId(e.responsibleUserId())
                .responsibleUserRole(e.responsibleUserRole())
                .neighbourhoodId(e.neighbourhoodId())
                .zoneId(e.zoneId())
                .resourceType(e.resourceType())
                .resourceId(e.resourceId())
                .payload(e.payload())
                .build();
        return repository.save(entity);
    }

    @Override
    public Page<? extends SystemTrailView> search(SystemTrailQuery query, Pageable pageable) {
        return repository.findAll(SystemTrailSpecifications.matching(query), pageable);
    }
}
