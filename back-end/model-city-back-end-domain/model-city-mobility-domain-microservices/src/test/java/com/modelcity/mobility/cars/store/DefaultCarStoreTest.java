package com.modelcity.mobility.cars.store;

import com.modelcity.mobility.cars.controller.model.CarRequestDto;
import com.modelcity.mobility.cars.repository.CarRepository;
import com.modelcity.mobility.cars.repository.model.Car;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultCarStoreTest {

    @Mock
    CarRepository<Car> carRepository;

    DefaultCarStore store;

    @BeforeEach
    void setUp() {
        store = new DefaultCarStore(carRepository);
    }

    @Test
    void create_buildsCarWithAllFields() {
        when(carRepository.save(any(Car.class))).thenAnswer(inv -> inv.getArgument(0));
        CarRequestDto request = new CarRequestDto();
        request.setNickname("Mi Coche");
        request.setBrand("Toyota");
        request.setModel("Corolla");

        Car result = store.create("owner-sub", "1234ABC", request);

        assertThat(result.getOwnerSub()).isEqualTo("owner-sub");
        assertThat(result.getLicensePlate()).isEqualTo("1234ABC");
        assertThat(result.getNickname()).isEqualTo("Mi Coche");
        assertThat(result.getBrand()).isEqualTo("Toyota");
        assertThat(result.getModel()).isEqualTo("Corolla");
    }

    @Test
    void existsByLicensePlate_delegatesToRepository() {
        when(carRepository.existsByLicensePlateIgnoreCase("1234ABC")).thenReturn(true);
        assertThat(store.existsByLicensePlate("1234ABC")).isTrue();
    }

    @Test
    void findById_delegatesToRepository() {
        store.findById(1L);
        verify(carRepository).findById(1L);
    }

    @Test
    void findByOwner_paginated_delegatesToRepository() {
        store.findByOwner("owner-sub", null);
        verify(carRepository).findByOwnerSubOrderByCreatedAtDesc("owner-sub", null);
    }

    @Test
    void findByOwner_list_delegatesToRepository() {
        store.findByOwner("owner-sub");
        verify(carRepository).findByOwnerSubOrderByCreatedAtDesc("owner-sub");
    }
}
