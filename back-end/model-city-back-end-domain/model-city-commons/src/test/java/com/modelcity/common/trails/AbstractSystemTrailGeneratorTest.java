package com.modelcity.common.trails;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbstractSystemTrailGeneratorTest {

    private static final String MDC_KEY = "modelCityCorrelationId";

    @Mock
    SystemTrailStore store;

    @Mock
    ObjectMapper failingObjectMapper;

    static class TestJacksonException extends JacksonException {
        TestJacksonException(String msg) {
            super(msg);
        }
    }

    static class TestGenerator extends AbstractSystemTrailGenerator {
        TestGenerator(SystemTrailStore store, ObjectMapper objectMapper) {
            super(store, objectMapper);
        }

        void emit(Object payload) {
            record("TEST_EVENT", OperationType.CREATE, "agent-sub", "MODEL-CITY-BACKOFFICE",
                    5L, 1L, "TEST_RESOURCE", "10", payload);
        }

        Map<String, Object> buildPayload(Object... kv) {
            return payload(kv);
        }
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void record_buildsEnvelopeWithCorrelationIdFromMdc() {
        MDC.put(MDC_KEY, "corr-42");
        TestGenerator generator = new TestGenerator(store, JsonMapper.builder().build());

        generator.emit(Map.of("key", "value"));

        ArgumentCaptor<NewSystemTrail> captor = ArgumentCaptor.forClass(NewSystemTrail.class);
        verify(store).save(captor.capture());
        NewSystemTrail event = captor.getValue();

        assertThat(event.eventId()).isNotNull();
        assertThat(event.eventType()).isEqualTo("TEST_EVENT");
        assertThat(event.operationType()).isEqualTo(OperationType.CREATE);
        assertThat(event.correlationId()).isEqualTo("corr-42");
        assertThat(event.responsibleUserId()).isEqualTo("agent-sub");
        assertThat(event.responsibleUserRole()).isEqualTo("MODEL-CITY-BACKOFFICE");
        assertThat(event.neighbourhoodId()).isEqualTo(5L);
        assertThat(event.zoneId()).isEqualTo(1L);
        assertThat(event.resourceType()).isEqualTo("TEST_RESOURCE");
        assertThat(event.resourceId()).isEqualTo("10");
        assertThat(event.payload()).contains("\"key\":\"value\"");
    }

    @Test
    void record_withoutMdcCorrelationId_leavesCorrelationIdNull() {
        TestGenerator generator = new TestGenerator(store, JsonMapper.builder().build());

        generator.emit(Map.of("key", "value"));

        ArgumentCaptor<NewSystemTrail> captor = ArgumentCaptor.forClass(NewSystemTrail.class);
        verify(store).save(captor.capture());
        assertThat(captor.getValue().correlationId()).isNull();
    }

    @Test
    void record_nullPayload_producesNullJson() {
        TestGenerator generator = new TestGenerator(store, JsonMapper.builder().build());

        generator.emit(null);

        ArgumentCaptor<NewSystemTrail> captor = ArgumentCaptor.forClass(NewSystemTrail.class);
        verify(store).save(captor.capture());
        assertThat(captor.getValue().payload()).isNull();
    }

    @Test
    void record_serializationFailure_recoversWithNullPayload() {
        when(failingObjectMapper.writeValueAsString(any())).thenThrow(new TestJacksonException("boom"));
        TestGenerator generator = new TestGenerator(store, failingObjectMapper);

        generator.emit(Map.of("key", "value"));

        ArgumentCaptor<NewSystemTrail> captor = ArgumentCaptor.forClass(NewSystemTrail.class);
        verify(store).save(captor.capture());
        assertThat(captor.getValue().payload()).isNull();
    }

    @Test
    void payload_pairsKeysAndValuesInOrder() {
        TestGenerator generator = new TestGenerator(store, JsonMapper.builder().build());

        Map<String, Object> result = generator.buildPayload("id", 1L, "name", "Plaza Mayor");

        assertThat(result).containsExactly(
                Map.entry("id", 1L), Map.entry("name", "Plaza Mayor"));
    }

    @Test
    void payload_allowsNullValues() {
        TestGenerator generator = new TestGenerator(store, JsonMapper.builder().build());

        Map<String, Object> result = generator.buildPayload("id", null);

        assertThat(result).containsEntry("id", null);
    }

    @Test
    void payload_oddNumberOfArgs_dropsTrailingKey() {
        TestGenerator generator = new TestGenerator(store, JsonMapper.builder().build());

        Map<String, Object> result = generator.buildPayload("id", 1L, "orphanKey");

        assertThat(result).containsOnly(Map.entry("id", 1L));
    }
}
