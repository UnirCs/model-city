package com.modelcity.common.i18n;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocaleExchangeFilterTest {

    private final ExchangeFilterFunction filter = LocaleExchangeFilter.create();

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void filter_propagatesCurrentLocaleAsAcceptLanguage() {
        LocaleContextHolder.setLocale(Locale.FRENCH);
        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("http://core/users/1")).build();
        ExchangeFunction next = mock(ExchangeFunction.class);
        when(next.exchange(any())).thenReturn(Mono.just(mock(ClientResponse.class)));

        filter.filter(request, next).block();

        ArgumentCaptor<ClientRequest> captor = ArgumentCaptor.forClass(ClientRequest.class);
        verify(next).exchange(captor.capture());
        assertThat(captor.getValue().headers().getFirst("Accept-Language")).isEqualTo("fr");
    }

    @Test
    void filter_unsupportedLocale_fallsBackToDefault() {
        LocaleContextHolder.setLocale(Locale.GERMAN);
        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("http://core/users/1")).build();
        ExchangeFunction next = mock(ExchangeFunction.class);
        when(next.exchange(any())).thenReturn(Mono.just(mock(ClientResponse.class)));

        filter.filter(request, next).block();

        ArgumentCaptor<ClientRequest> captor = ArgumentCaptor.forClass(ClientRequest.class);
        verify(next).exchange(captor.capture());
        assertThat(captor.getValue().headers().getFirst("Accept-Language")).isEqualTo("es");
    }
}
