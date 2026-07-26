package com.modelcity.common.observability.filter;

import com.modelcity.common.observability.model.CorrelationId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CorrelationIdExchangeFilterTest {

    private final ExchangeFilterFunction filter = CorrelationIdExchangeFilter.create();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void filter_withMdcCorrelationId_addsHeaderToOutgoingRequest() {
        MDC.put(CorrelationId.MDC_KEY, "corr-123");
        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("http://core/users/1")).build();
        ExchangeFunction next = mock(ExchangeFunction.class);
        when(next.exchange(any())).thenReturn(Mono.just(mock(ClientResponse.class)));

        filter.filter(request, next).block();

        ArgumentCaptor<ClientRequest> captor = ArgumentCaptor.forClass(ClientRequest.class);
        verify(next).exchange(captor.capture());
        assertThat(captor.getValue().headers().getFirst(CorrelationId.HEADER)).isEqualTo("corr-123");
    }

    @Test
    void filter_withoutMdcCorrelationId_leavesRequestUnmodified() {
        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("http://core/users/1")).build();
        ExchangeFunction next = mock(ExchangeFunction.class);
        when(next.exchange(any())).thenReturn(Mono.just(mock(ClientResponse.class)));

        filter.filter(request, next).block();

        ArgumentCaptor<ClientRequest> captor = ArgumentCaptor.forClass(ClientRequest.class);
        verify(next).exchange(captor.capture());
        assertThat(captor.getValue().headers().getFirst(CorrelationId.HEADER)).isNull();
    }
}
