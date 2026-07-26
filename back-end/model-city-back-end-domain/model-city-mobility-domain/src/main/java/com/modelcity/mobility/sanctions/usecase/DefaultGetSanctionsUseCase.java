package com.modelcity.mobility.sanctions.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;

import com.modelcity.mobility.sanctions.controller.model.SanctionRequestDto;
import com.modelcity.mobility.sanctions.controller.model.SanctionSummaryDto;
import com.modelcity.mobility.sanctions.store.SanctionStore;
import com.modelcity.mobility.sanctions.store.model.SanctionView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Default {@link GetSanctionsUseCase} implementation. Component-scanned platform default; disabled at startup when a
 * local deployment provides its own bean for the seam.
 *
 * <p>Doubles as an <strong>extension base</strong>: a local deployment may either implement
 * {@link GetSanctionsUseCase} from scratch, or {@code extends} this default and {@code @Override} it, calling
 * {@code super.execute(...)} to reuse the platform behaviour. The {@code protected} store is reachable by a
 * subclass. Either way the default backs off, since a subclass is still a {@code GetSanctionsUseCase} bean.
 */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetSanctionsUseCase implements GetSanctionsUseCase<SanctionSummaryDto> {

    private static final int PAGE_SIZE = 20;

    protected final SanctionStore<? extends SanctionView, SanctionRequestDto> sanctionStore;

    @Override
    @Transactional(readOnly = true)
    public Page<SanctionSummaryDto> execute(String licensePlate, OffsetDateTime from, OffsetDateTime to, int page) {
        PageRequest pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("createdAt").descending());
        return sanctionStore.search(licensePlate, from, to, pageable).map(SanctionSummaryDto::from);
    }
}
