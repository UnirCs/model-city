package com.modelcity.leisure.trails.store;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Component;
import com.modelcity.common.trails.NewSystemTrail;
import com.modelcity.common.trails.SystemTrailQuery;
import com.modelcity.common.trails.SystemTrailSpecifications;
import com.modelcity.common.trails.SystemTrailView;
import com.modelcity.leisure.trails.repository.LeisureTrailRepository;
import com.modelcity.leisure.trails.repository.model.LeisureTrail;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** JPA adapter for the leisure system-trail port. */
@RequiredArgsConstructor
@Component
@ModelCityDisabledIfInherited
public class DefaultLeisureSystemTrailStore implements LeisureSystemTrailStore {

    private final LeisureTrailRepository<LeisureTrail> repository;

    @Override
    public SystemTrailView save(NewSystemTrail e) {
        LeisureTrail entity = LeisureTrail.builder()
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
