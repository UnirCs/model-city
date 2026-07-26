package com.modelcity.common.observability.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class HttpLoggingFilterTest {

    private final HttpLoggingFilter filter = new HttpLoggingFilter();
    private final Logger logger = (Logger) LoggerFactory.getLogger(HttpLoggingFilter.class);
    private final Level originalLevel = logger.getLevel();

    @AfterEach
    void tearDown() {
        logger.setLevel(originalLevel);
    }

    @Test
    void doFilterInternal_debugDisabled_passesThroughOriginalRequestAndResponse() throws Exception {
        logger.setLevel(Level.INFO);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/city-places");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        var captor = org.mockito.ArgumentCaptor.forClass(jakarta.servlet.ServletRequest.class);
        verify(chain).doFilter(captor.capture(), any());
        assertThat(captor.getValue()).isSameAs(request);
    }

    @Test
    void doFilterInternal_debugEnabled_wrapsRequestAndResponseForCaching() throws Exception {
        logger.setLevel(Level.DEBUG);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/city-places");
        request.setContent("{\"name\":\"test\"}".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        var requestCaptor = org.mockito.ArgumentCaptor.forClass(jakarta.servlet.ServletRequest.class);
        var responseCaptor = org.mockito.ArgumentCaptor.forClass(jakarta.servlet.ServletResponse.class);
        verify(chain).doFilter(requestCaptor.capture(), responseCaptor.capture());
        assertThat(requestCaptor.getValue()).isInstanceOf(ContentCachingRequestWrapper.class);
        assertThat(responseCaptor.getValue()).isInstanceOf(ContentCachingResponseWrapper.class);
    }
}
