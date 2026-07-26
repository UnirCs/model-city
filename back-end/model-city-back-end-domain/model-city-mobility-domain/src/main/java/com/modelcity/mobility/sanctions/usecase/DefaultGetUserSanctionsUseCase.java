package com.modelcity.mobility.sanctions.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;

import com.modelcity.mobility.cars.controller.model.CarRequestDto;
import com.modelcity.mobility.cars.store.model.CarView;
import com.modelcity.mobility.cars.store.CarStore;
import com.modelcity.mobility.sanctions.controller.model.SanctionRequestDto;
import com.modelcity.mobility.sanctions.controller.model.SanctionSummaryDto;
import com.modelcity.mobility.sanctions.store.SanctionStore;
import com.modelcity.mobility.sanctions.store.model.SanctionView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Default {@link GetUserSanctionsUseCase} implementation. Component-scanned platform default; disabled at startup when a
 * local deployment provides its own bean for the seam.
 *
 * <p>Doubles as an <strong>extension base</strong>: a local deployment may either implement
 * {@link GetUserSanctionsUseCase} from scratch, or {@code extends} this default and {@code @Override} it,
 * calling {@code super.execute(...)} to reuse the platform behaviour. Collaborators are {@code protected} so a
 * subclass can reach them. Either way the default backs off, since a subclass is still a
 * {@code GetUserSanctionsUseCase} bean.
 */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetUserSanctionsUseCase implements GetUserSanctionsUseCase<SanctionSummaryDto> {

    protected final CarStore<? extends CarView, CarRequestDto> carStore;
    protected final SanctionStore<? extends SanctionView, SanctionRequestDto> sanctionStore;

    @Override
    @Cacheable(cacheNames = CacheNames.USER_SANCTIONS,
            key = "#userId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<SanctionSummaryDto> execute(String userId, String sub, Pageable pageable) {
        if (!userId.equals(sub)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Caller does not match path user id");
        }
        List<String> plates = carStore.findByOwner(sub).stream()
                .map(CarView::getLicensePlate)
                .toList();
        if (plates.isEmpty()) return Page.empty(pageable);

        PageRequest sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdAt").descending());
        return sanctionStore.findByPlatesIn(plates, sorted).map(SanctionSummaryDto::from);
    }
}
