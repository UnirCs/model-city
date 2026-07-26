package com.modelcity.mobility.cars.controller;

import com.modelcity.mobility.cars.controller.model.CarDto;
import com.modelcity.mobility.cars.controller.model.CarRequestDto;
import com.modelcity.mobility.cars.usecase.CreateCarUseCase;
import com.modelcity.mobility.cars.usecase.GetUserCarsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserCarControllerTest {

    @Mock CreateCarUseCase<CarDto, CarRequestDto> createCarUseCase;
    @Mock GetUserCarsUseCase<CarDto> getUserCarsUseCase;

    DefaultUserCarController controller;

    @BeforeEach
    void setUp() {
        controller = new DefaultUserCarController(createCarUseCase, getUserCarsUseCase);
    }

    @Test
    void createCar_delegatesToUseCase() {
        CarRequestDto request = new CarRequestDto();
        controller.createCar("user-sub", "user-sub", request);
        verify(createCarUseCase).execute("user-sub", "user-sub", request);
    }

    @Test
    void getCars_delegatesToUseCase() {
        PageRequest pageable = PageRequest.of(0, 5);
        controller.getCars("user-sub", "user-sub", pageable);
        verify(getUserCarsUseCase).execute("user-sub", "user-sub", pageable);
    }
}
