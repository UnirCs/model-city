package com.modelcity.mobility.cars.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;

import com.modelcity.mobility.cars.controller.model.CarDto;
import com.modelcity.mobility.cars.controller.model.CarRequestDto;
import com.modelcity.mobility.cars.store.model.CarView;
import com.modelcity.mobility.cars.store.CarStore;
import com.modelcity.mobility.trails.SystemTrailGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Default {@link CreateCarUseCase} implementation. Component-scanned platform default; disabled at startup when a
 * local deployment provides its own bean for the seam.
 *
 * <p>Doubles as an <strong>extension base</strong>: a local deployment may either implement
 * {@link CreateCarUseCase} from scratch, or {@code extends} this default and {@code @Override} it, calling
 * {@code super.execute(...)} to reuse the platform behaviour. Collaborators are {@code protected} so a
 * subclass can reach them. Either way the default backs off, since a subclass is still a
 * {@code CreateCarUseCase} bean.
 */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultCreateCarUseCase implements CreateCarUseCase<CarDto, CarRequestDto> {

    protected final CarStore<? extends CarView, CarRequestDto> carStore;
    protected final SystemTrailGenerator systemEventGenerator;

    @Override
    @CacheEvict(cacheNames = CacheNames.USER_CARS, key = "#userId + ':0:5'")
    public CarDto execute(String userId, String sub, CarRequestDto request) {
        if (!userId.equals(sub)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Caller does not match path user id");
        }
        String plate = request.getLicensePlate().trim().toUpperCase();
        if (carStore.existsByLicensePlate(plate)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "License plate already registered");
        }
        CarView saved = carStore.create(sub, plate, request);
        systemEventGenerator.carRegistered(saved);
        log.info("Car id={} plate={} created for sub={}", saved.getId(), plate, sub);
        return CarDto.from(saved);
    }
}
