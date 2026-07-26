package com.modelcity.leisure.trails.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;
import com.modelcity.common.trails.SystemTrailDto;
import com.modelcity.common.trails.SystemTrailQuery;
import com.modelcity.leisure.trails.store.LeisureSystemTrailStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link GetSystemTrailsUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@RequiredArgsConstructor
@Service("leisureDefaultGetSystemTrailsUseCase")
@ModelCityDisabledIfInherited
public class DefaultGetSystemTrailsUseCase implements GetSystemTrailsUseCase {

    private static final int PAGE_SIZE = 20;

    private final LeisureSystemTrailStore store;

    @Override
    @Transactional(readOnly = true)
    public Page<SystemTrailDto> execute(SystemTrailQuery query, int page) {
        PageRequest pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("occurredAt").descending());
        return store.search(query, pageable).map(SystemTrailDto::from);
    }
}
