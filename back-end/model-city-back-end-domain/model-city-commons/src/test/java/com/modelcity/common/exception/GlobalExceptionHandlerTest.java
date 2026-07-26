package com.modelcity.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private HttpServletRequest mockRequest(String method, String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn(method);
        when(request.getRequestURI()).thenReturn(uri);
        return request;
    }

    @Test
    void handleResponseStatus_withReason_usesReasonAsMessage() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "City place not found");
        HttpServletRequest request = mockRequest("GET", "/city-places/99");

        ResponseEntity<ApiErrorResponse> response = handler.handleResponseStatus(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().message()).isEqualTo("City place not found");
        assertThat(response.getBody().path()).isEqualTo("/city-places/99");
    }

    @Test
    void handleResponseStatus_withoutReason_fallsBackToReasonPhrase() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.FORBIDDEN);
        HttpServletRequest request = mockRequest("DELETE", "/sanctions/1");

        ResponseEntity<ApiErrorResponse> response = handler.handleResponseStatus(ex, request);

        assertThat(response.getBody().message()).isEqualTo("Forbidden");
    }

    @Test
    void handleResponseStatus_withCustomStatusCode_resolvesGenericErrorLabel() {
        ResponseStatusException ex = new ResponseStatusException(org.springframework.http.HttpStatusCode.valueOf(499));
        HttpServletRequest request = mockRequest("GET", "/foo");

        ResponseEntity<ApiErrorResponse> response = handler.handleResponseStatus(ex, request);

        assertThat(response.getBody().error()).isEqualTo("Error");
    }

    @Test
    void handleValidation_joinsFieldErrorMessages() throws NoSuchMethodException {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "request");
        bindingResult.addError(new FieldError("request", "name", "must not be blank"));
        bindingResult.addError(new FieldError("request", "email", "must be a valid email"));

        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("mockRequest", String.class, String.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(method, 0), bindingResult);
        HttpServletRequest request = mockRequest("POST", "/city-places");

        ResponseEntity<ApiErrorResponse> response = handler.handleValidation(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().message())
                .contains("must not be blank")
                .contains("must be a valid email");
    }

    @Test
    void handleGeneric_returnsInternalServerErrorWithGenericMessage() {
        HttpServletRequest request = mockRequest("GET", "/boom");

        ResponseEntity<ApiErrorResponse> response = handler.handleGeneric(new RuntimeException("db down"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
    }
}
