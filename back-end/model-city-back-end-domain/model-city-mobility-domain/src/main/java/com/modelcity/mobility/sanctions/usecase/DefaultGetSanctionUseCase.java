package com.modelcity.mobility.sanctions.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.mobility.sanctions.controller.model.SanctionDto;
import com.modelcity.mobility.sanctions.controller.model.SanctionRequestDto;
import com.modelcity.mobility.sanctions.store.SanctionStore;
import com.modelcity.mobility.sanctions.store.model.SanctionView;
import lombok.RequiredArgsConstructor;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link GetSanctionUseCase} implementation. Component-scanned platform default; disabled at startup when a
 * local deployment provides its own bean for the seam.
 *
 * <p>Doubles as an <strong>extension base</strong>: a local deployment may either implement
 * {@link GetSanctionUseCase} from scratch, or {@code extends} this default and {@code @Override} it, calling
 * {@code super.execute(...)} to reuse the platform behaviour. The {@code protected} store is reachable by a
 * subclass. Either way the default backs off, since a subclass is still a {@code GetSanctionUseCase} bean.
 */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetSanctionUseCase implements GetSanctionUseCase<SanctionDto> {

    protected final SanctionStore<? extends SanctionView, SanctionRequestDto> sanctionStore;

    @Override
    @Cacheable(cacheNames = CacheNames.SANCTION, key = "#id")
    @Transactional(readOnly = true)
    public SanctionDto execute(Long id) {
        return sanctionStore.findById(id)
                .map(SanctionDto::from)
                .orElseThrow(() -> new ResourceNotFoundException("Sanction", id));
    }
}
