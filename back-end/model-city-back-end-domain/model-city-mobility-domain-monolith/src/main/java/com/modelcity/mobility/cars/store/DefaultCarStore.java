package com.modelcity.mobility.cars.store;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import com.modelcity.mobility.cars.controller.model.CarRequestDto;
import com.modelcity.mobility.cars.repository.CarRepository;
import com.modelcity.mobility.cars.repository.model.Car;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter for the car persistence port; the default {@link CarStore} bean.
 *
 * <p>Doubles as an <strong>extension base</strong>: a local deployment may either implement {@link CarStore}
 * from scratch, or {@code extends} this default and {@code @Override} only the operations it needs, calling
 * {@code super} for the rest. The {@code protected} repository is reachable so a subclass can add its own
 * queries. Either way the platform default backs off, since a subclass is still a {@code CarStore} bean.
 */
@Component
@ModelCityDisabledIfInherited
@RequiredArgsConstructor
public class DefaultCarStore implements CarStore<Car, CarRequestDto> {

    protected final CarRepository<Car> carRepository;

    @Override
    public boolean existsByLicensePlate(String licensePlate) {
        return carRepository.existsByLicensePlateIgnoreCase(licensePlate);
    }

    @Override
    public Car create(String ownerSub, String licensePlate, CarRequestDto request) {
        Car car = Car.builder()
                .ownerSub(ownerSub)
                .licensePlate(licensePlate)
                .nickname(request.getNickname())
                .brand(request.getBrand())
                .model(request.getModel())
                .build();
        return carRepository.save(car);
    }

    @Override
    public Optional<Car> findById(Long id) {
        return carRepository.findById(id);
    }

    @Override
    public Page<Car> findByOwner(String ownerSub, Pageable pageable) {
        return carRepository.findByOwnerSubOrderByCreatedAtDesc(ownerSub, pageable);
    }

    @Override
    public List<Car> findByOwner(String ownerSub) {
        return carRepository.findByOwnerSubOrderByCreatedAtDesc(ownerSub);
    }
}
