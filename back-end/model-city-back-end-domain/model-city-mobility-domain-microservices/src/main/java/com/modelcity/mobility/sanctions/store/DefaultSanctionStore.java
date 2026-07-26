package com.modelcity.mobility.sanctions.store;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import com.modelcity.mobility.sanctions.controller.model.SanctionRequestDto;
import com.modelcity.mobility.sanctions.repository.SanctionRepository;
import com.modelcity.mobility.sanctions.repository.SanctionSpecs;
import com.modelcity.mobility.sanctions.repository.model.Sanction;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;

/**
 * JPA adapter for the sanction persistence port; the default {@link SanctionStore} bean.
 *
 * <p>Doubles as an <strong>extension base</strong>: a local deployment may either implement
 * {@link SanctionStore} from scratch, or {@code extends} this default and {@code @Override} only the
 * operations it needs, calling {@code super} for the rest. The {@code protected} repository is reachable so a
 * subclass can add its own queries. Either way the platform default backs off, since a subclass is still a
 * {@code SanctionStore} bean.
 */
@Component
@ModelCityDisabledIfInherited
@RequiredArgsConstructor
public class DefaultSanctionStore implements SanctionStore<Sanction, SanctionRequestDto> {

    protected final SanctionRepository<Sanction> sanctionRepository;

    @Override
    public Sanction create(String agentSub, SanctionRequestDto request) {
        Sanction sanction = Sanction.builder()
                .licensePlate(request.getLicensePlate().trim().toUpperCase())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .imageBase64(request.getImageBase64())
                .agentSub(agentSub)
                .createdAt(OffsetDateTime.now())
                .build();
        return sanctionRepository.save(sanction);
    }

    @Override
    public Optional<Sanction> findById(Long id) {
        return sanctionRepository.findById(id);
    }

    @Override
    public Page<Sanction> search(String licensePlate, OffsetDateTime from, OffsetDateTime to, Pageable pageable) {
        Specification<Sanction> spec = and(
                SanctionSpecs.licensePlateEquals(licensePlate),
                SanctionSpecs.createdBetween(from, to));
        return sanctionRepository.findAll(spec, pageable);
    }

    @Override
    public Page<Sanction> findByPlatesIn(Collection<String> plates, Pageable pageable) {
        return sanctionRepository.findAll(SanctionSpecs.<Sanction>licensePlateIn(plates), pageable);
    }

    private static Specification<Sanction> and(Specification<Sanction> a, Specification<Sanction> b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.and(b);
    }
}
