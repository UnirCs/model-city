package com.modelcity.common.observability.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdTest {

    @Test
    void resolveOrCreate_incomingPresent_reusesIt() {
        assertThat(CorrelationId.resolveOrCreate("existing-id")).isEqualTo("existing-id");
    }

    @Test
    void resolveOrCreate_incomingNullOrBlank_generatesNewId() {
        String fromNull = CorrelationId.resolveOrCreate(null);
        String fromBlank = CorrelationId.resolveOrCreate("   ");

        assertThat(fromNull).isNotBlank();
        assertThat(fromBlank).isNotBlank();
        assertThat(fromNull).isNotEqualTo(fromBlank);
    }
}
