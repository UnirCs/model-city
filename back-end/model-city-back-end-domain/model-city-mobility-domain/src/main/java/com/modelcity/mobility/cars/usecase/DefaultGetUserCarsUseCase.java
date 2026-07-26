package com.modelcity.mobility.cars.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;

import com.modelcity.mobility.cars.controller.model.CarDto;
import com.modelcity.mobility.cars.controller.model.CarRequestDto;
import com.modelcity.mobility.cars.store.CarStore;
import com.modelcity.mobility.cars.store.model.CarView;
import lombok.RequiredArgsConstructor;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Default {@link GetUserCarsUseCase} implementation. Component-scanned platform default; disabled at startup when a
 * local deployment provides its own bean for the seam.
 *
 * <p>Doubles as an <strong>extension base</strong>: a local deployment may either implement
 * {@link GetUserCarsUseCase} from scratch, or {@code extends} this default and {@code @Override} it, calling
 * {@code super.execute(...)} to reuse the platform behaviour. The {@code protected} store is reachable by a
 * subclass. Either way the default backs off, since a subclass is still a {@code GetUserCarsUseCase} bean.
 */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetUserCarsUseCase implements GetUserCarsUseCase<CarDto> {

    protected final CarStore<? extends CarView, CarRequestDto> carStore;

    @Override
    @Cacheable(cacheNames = CacheNames.USER_CARS,
            key = "#userId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<CarDto> execute(String userId, String sub, Pageable pageable) {
        if (!userId.equals(sub)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Caller does not match path user id");
        }
        return carStore.findByOwner(userId, pageable).map(CarDto::from);
    }
}
