package com.modelcity.mobility.trails.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;

import com.modelcity.common.trails.SystemTrailDto;
import com.modelcity.common.trails.SystemTrailQuery;
import com.modelcity.mobility.trails.store.MobilitySystemTrailStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link GetSystemTrailsUseCase} implementation. Component-scanned platform default; disabled at startup when a
 * local deployment provides its own bean for the seam.
 *
 * <p>Doubles as an <strong>extension base</strong>: a local deployment may either implement
 * {@link GetSystemTrailsUseCase} from scratch, or {@code extends} this default and {@code @Override} it,
 * calling {@code super.execute(...)} to reuse the platform behaviour. The {@code protected} store is reachable
 * by a subclass. Either way the default backs off, since a subclass is still a {@code GetSystemTrailsUseCase}
 * bean.
 */
@RequiredArgsConstructor
@Service("mobilityDefaultGetSystemTrailsUseCase")
@ModelCityDisabledIfInherited
public class DefaultGetSystemTrailsUseCase implements GetSystemTrailsUseCase {

    private static final int PAGE_SIZE = 20;

    protected final MobilitySystemTrailStore store;

    @Override
    @Transactional(readOnly = true)
    public Page<SystemTrailDto> execute(SystemTrailQuery query, int page) {
        PageRequest pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("occurredAt").descending());
        return store.search(query, pageable).map(SystemTrailDto::from);
    }
}
