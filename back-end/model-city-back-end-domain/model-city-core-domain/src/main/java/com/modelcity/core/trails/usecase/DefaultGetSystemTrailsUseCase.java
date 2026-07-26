package com.modelcity.core.trails.usecase;

import com.modelcity.common.trails.SystemTrailDto;
import com.modelcity.common.trails.SystemTrailQuery;
import com.modelcity.core.trails.store.CoreSystemTrailStore;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link GetSystemTrailsUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@RequiredArgsConstructor
@Service("coreDefaultGetSystemTrailsUseCase")
@ModelCityDisabledIfInherited
public class DefaultGetSystemTrailsUseCase implements GetSystemTrailsUseCase {

    private static final int PAGE_SIZE = 20;

    private final CoreSystemTrailStore store;

    @Override
    @Transactional(readOnly = true)
    public Page<SystemTrailDto> execute(SystemTrailQuery query, int page) {
        PageRequest pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("occurredAt").descending());
        return store.search(query, pageable).map(SystemTrailDto::from);
    }
}
