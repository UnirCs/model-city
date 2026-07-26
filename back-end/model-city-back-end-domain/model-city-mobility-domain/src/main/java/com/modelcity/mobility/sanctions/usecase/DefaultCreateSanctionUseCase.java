package com.modelcity.mobility.sanctions.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;

import com.modelcity.mobility.sanctions.controller.model.SanctionDto;
import com.modelcity.mobility.sanctions.controller.model.SanctionRequestDto;
import com.modelcity.mobility.sanctions.store.model.SanctionView;
import com.modelcity.mobility.sanctions.store.SanctionStore;
import com.modelcity.mobility.trails.SystemTrailGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link CreateSanctionUseCase} implementation. Component-scanned platform default; disabled at startup when a
 * local deployment provides its own bean for the seam.
 *
 * <p>Doubles as an <strong>extension base</strong>: a local deployment may either implement
 * {@link CreateSanctionUseCase} from scratch, or {@code extends} this default and {@code @Override} it,
 * calling {@code super.execute(...)} to reuse the platform behaviour (persist + audit event) and layer its own
 * logic on top. Collaborators are {@code protected} so a subclass can reach them. Either way the default backs
 * off, since a subclass is still a {@code CreateSanctionUseCase} bean.
 */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultCreateSanctionUseCase implements CreateSanctionUseCase<SanctionDto, SanctionRequestDto> {

    protected final SanctionStore<? extends SanctionView, SanctionRequestDto> sanctionStore;
    protected final SystemTrailGenerator systemEventGenerator;

    @Override
    @CacheEvict(cacheNames = CacheNames.USER_SANCTIONS, allEntries = true)
    public SanctionDto execute(String agentSub, SanctionRequestDto request) {
        SanctionView saved = sanctionStore.create(agentSub, request);
        systemEventGenerator.sanctionIssued(saved);
        log.info("Sanction id={} issued by sub={} plate={}", saved.getId(), agentSub, saved.getLicensePlate());
        return SanctionDto.from(saved);
    }
}
