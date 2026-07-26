package com.modelcity.engagement.trails.store;

import com.modelcity.engagement.trails.repository.EngagementTrailRepository;
import com.modelcity.engagement.trails.repository.model.EngagementTrail;
import com.modelcity.common.trails.NewSystemTrail;
import com.modelcity.common.trails.SystemTrailQuery;
import com.modelcity.common.trails.SystemTrailSpecifications;
import com.modelcity.common.trails.SystemTrailView;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * JPA adapter for the engagement system-trail port; the default {@link EngagementSystemTrailStore}
 * bean.
 */
@Component
@ModelCityDisabledIfInherited
@RequiredArgsConstructor
public class DefaultEngagementSystemTrailStore implements EngagementSystemTrailStore {

    private final EngagementTrailRepository<EngagementTrail> repository;

    @Override
    public SystemTrailView save(NewSystemTrail e) {
        EngagementTrail entity = EngagementTrail.builder()
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
