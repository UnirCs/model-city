package com.modelcity.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test de arranque mínimo. El contexto completo de Spring (con BD y Eureka)
 * se valida en tests de integración con @SpringBootTest y Testcontainers.
 */
class CoreApplicationTest {

    @Test
    void contextLoads() {
        assertTrue(true, "core module compiled successfully");
    }
}

