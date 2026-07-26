package com.modelcity.leisure.events.store;

import com.modelcity.leisure.events.repository.EventRefundRepository;
import com.modelcity.leisure.events.repository.model.EventRefund;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultEventRefundStoreTest {

    @Mock
    EventRefundRepository<EventRefund> eventRefundRepository;

    DefaultEventRefundStore store;

    @BeforeEach
    void setUp() {
        store = new DefaultEventRefundStore(eventRefundRepository);
    }

    @Test
    void create_buildsRefundWithAllFieldsAndPersists() {
        when(eventRefundRepository.save(any(EventRefund.class))).thenAnswer(inv -> inv.getArgument(0));

        EventRefund result = store.create(10L, BigDecimal.TEN, "EUR", "event cancelled", true, "agent-sub");

        assertThat(result.getTicketId()).isEqualTo(10L);
        assertThat(result.getAmount()).isEqualTo(BigDecimal.TEN);
        assertThat(result.getCurrency()).isEqualTo("EUR");
        assertThat(result.getReason()).isEqualTo("event cancelled");
        assertThat(result.isAutomatic()).isTrue();
        assertThat(result.getIssuedBySub()).isEqualTo("agent-sub");
    }

    @Test
    void create_delegatesPersistenceToRepository() {
        ArgumentCaptor<EventRefund> captor = ArgumentCaptor.forClass(EventRefund.class);
        when(eventRefundRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        store.create(10L, BigDecimal.ONE, "EUR", "manual", false, "sub");

        verify(eventRefundRepository).save(any(EventRefund.class));
        assertThat(captor.getValue().isAutomatic()).isFalse();
    }
}
