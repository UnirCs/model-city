package com.modelcity.mobility.sanctions.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.mobility.cars.controller.model.CarRequestDto;
import com.modelcity.mobility.cars.store.CarStore;
import com.modelcity.mobility.cars.store.model.CarView;
import com.modelcity.mobility.sanctions.controller.model.SanctionDto;
import com.modelcity.mobility.sanctions.controller.model.SanctionRequestDto;
import com.modelcity.mobility.sanctions.store.model.SanctionView;
import com.modelcity.mobility.sanctions.store.SanctionStore;
import lombok.RequiredArgsConstructor;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default {@link GetUserSanctionUseCase} implementation. Component-scanned platform default; disabled at startup when a
 * local deployment provides its own bean for the seam.
 *
 * <p>Doubles as an <strong>extension base</strong>: a local deployment may either implement
 * {@link GetUserSanctionUseCase} from scratch, or {@code extends} this default and {@code @Override} it,
 * calling {@code super.execute(...)} to reuse the platform behaviour. Collaborators are {@code protected} so a
 * subclass can reach them. Either way the default backs off, since a subclass is still a
 * {@code GetUserSanctionUseCase} bean.
 */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetUserSanctionUseCase implements GetUserSanctionUseCase<SanctionDto> {

    protected final CarStore<? extends CarView, CarRequestDto> carStore;
    protected final SanctionStore<? extends SanctionView, SanctionRequestDto> sanctionStore;

    @Override
    @Cacheable(cacheNames = CacheNames.SANCTION, key = "#sanctionId")
    @Transactional(readOnly = true)
    public SanctionDto execute(String userId, String sub, Long sanctionId) {
        if (!userId.equals(sub)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Caller does not match path user id");
        }
        SanctionView sanction = sanctionStore.findById(sanctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sanction", sanctionId));
        Set<String> plates = carStore.findByOwner(sub).stream()
                .map(c -> c.getLicensePlate().toUpperCase())
                .collect(Collectors.toSet());
        if (!plates.contains(sanction.getLicensePlate().toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sanction does not belong to caller");
        }
        return SanctionDto.from(sanction);
    }
}
