package com.modelcity.mobility.cars.repository;

import com.modelcity.mobility.cars.repository.model.Car;

/** Concrete Spring Data repository binding {@link CarRepository} to this topology's {@code Car}. */
public interface DefaultCarRepository extends CarRepository<Car> {
}
