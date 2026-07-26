package com.modelcity.mobility.trails.store;

import com.modelcity.common.trails.NewSystemTrail;
import com.modelcity.common.trails.SystemTrailQuery;
import com.modelcity.common.trails.SystemTrailSpecifications;
import com.modelcity.common.trails.SystemTrailView;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import com.modelcity.mobility.trails.repository.MobilityTrailRepository;
import com.modelcity.mobility.trails.repository.model.MobilityTrail;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Monolith JPA adapter for the mobility system-trail port; the default {@link MobilitySystemTrailStore} bean.
 *
 * <p>Doubles as an <strong>extension base</strong>: a local deployment may either implement
 * {@link MobilitySystemTrailStore} from scratch, or {@code extends} this default and {@code @Override} only
 * the operations it needs, calling {@code super} for the rest (the {@code protected} repository is reachable).
 * Either way the platform default backs off, since a subclass is still a {@code MobilitySystemTrailStore} bean.
 */
@Component
@ModelCityDisabledIfInherited
@RequiredArgsConstructor
public class DefaultMobilitySystemTrailStore implements MobilitySystemTrailStore {

    protected final MobilityTrailRepository<MobilityTrail> repository;

    @Override
    public SystemTrailView save(NewSystemTrail e) {
        MobilityTrail entity = MobilityTrail.builder()
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
