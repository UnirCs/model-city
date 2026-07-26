package com.modelcity.common.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceNotFoundExceptionTest {

    @Test
    void constructor_withResourceNameAndId_formatsMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("CityPlace", 42L);

        assertThat(ex.getMessage()).isEqualTo("CityPlace with id '42' not found");
        assertThat(ex.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
    }

    @Test
    void constructor_withPlainMessage_usesMessageVerbatim() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Custom message");

        assertThat(ex.getMessage()).isEqualTo("Custom message");
        assertThat(ex.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
    }

    @Test
    void isModelCityException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User", "auth0|123");
        assertThat(ex).isInstanceOf(ModelCityException.class);
    }
}
