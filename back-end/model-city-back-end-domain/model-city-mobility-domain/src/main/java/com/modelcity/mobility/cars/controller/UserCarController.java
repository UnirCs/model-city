package com.modelcity.mobility.cars.controller;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.mobility.cars.controller.model.CarDto;
import com.modelcity.mobility.cars.controller.model.CarRequestDto;
import com.modelcity.mobility.cars.usecase.CreateCarUseCase;
import com.modelcity.mobility.cars.usecase.GetUserCarsUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints for managing a citizen's vehicles.
 *
 * <p>Overridable base controller (abstract): the platform registers {@link DefaultUserCarController} as the
 * default bean. A local deployment overrides by declaring its own {@code @RestController} that
 * {@code extends UserCarController}; the default then backs off.
 */
@Slf4j
@RestController
@RequestMapping("/users/{userId}/cars")
@RequiredArgsConstructor
@ModelCityExtensionPoint
public abstract class UserCarController<T extends CarDto, R extends CarRequestDto> {

    protected final CreateCarUseCase<T, R> createCarUseCase;
    protected final GetUserCarsUseCase<T> getUserCarsUseCase;

    /** Registers a new car for the given citizen. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public T createCar(
            @PathVariable String userId,
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid R request) {
        log.debug("POST /users/{}/cars sub={}", userId, sub);
        return createCarUseCase.execute(userId, sub, request);
    }

    /** Lists all cars of the given citizen. */
    @GetMapping
    public Page<T> getCars(
            @PathVariable String userId,
            @RequestHeader("X-Auth-Sub") String sub,
            @PageableDefault(size = 5) Pageable pageable) {
        log.debug("GET /users/{}/cars sub={}", userId, sub);
        return getUserCarsUseCase.execute(userId, sub, pageable);
    }
}
