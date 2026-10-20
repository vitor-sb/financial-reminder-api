package com.reminder.financial.api.adapters.in.web.exception;

import com.reminder.financial.api.domains.exceptions.ConflictException;
import com.reminder.financial.api.domains.exceptions.NotFoundException;
import com.reminder.financial.api.domains.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApplicationExceptionHandlerTest {

    private final ApplicationExceptionHandler exceptionHandler = new ApplicationExceptionHandler();

    @Test
    @DisplayName("Should handle NotFoundException and return appropriate ErrorDTO")
    public void testHandleNotFound() {
        NotFoundException exception = new NotFoundException("Resource not found");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn("/test");

        ErrorResponse response = exceptionHandler.handleNotFound(exception, request);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.status());
        assertEquals(HttpStatus.NOT_FOUND.name(), response.error());
        assertEquals("Resource not found", response.message());
        assertEquals("/test", response.path());
    }

    @Test
    @DisplayName("Should handle UnauthorizedException and return appropriate ErrorDTO")
    public void testHandleUnauthorized() {
        UnauthorizedException exception = new UnauthorizedException("Unauthorized access");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn("/test");

        ErrorResponse response = exceptionHandler.handleUnauthorized(exception, request);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.status());
        assertEquals(HttpStatus.UNAUTHORIZED.name(), response.error());
        assertEquals("Unauthorized access", response.message());
        assertEquals("/test", response.path());
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException and return appropriate ErrorDTO")
    public void testHandleValidationError() {
        BindingResult bindingResult = mock(BindingResult.class);

        when(bindingResult.getFieldErrors()).thenReturn(
                List.of(
                        new org.springframework.validation.FieldError("objectName", "fieldOne", "First default message"),
                        new org.springframework.validation.FieldError("objectName", "fieldTwo", "Second default message")
                )
        );

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn("/test");

        ErrorResponse response = exceptionHandler.handleValidationError(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status());
        assertEquals(HttpStatus.BAD_REQUEST.name(), response.error());
        assertEquals("/test", response.path());

        Map<String, String> expectedErrors = new HashMap<>();
        expectedErrors.put("fieldOne", "First default message");
        expectedErrors.put("fieldTwo", "Second default message");
        String expectedMessage = expectedErrors.toString();

        assertEquals(expectedMessage, response.message());
    }

    @Test
    @DisplayName("Should handle Exception and return appropriate ErrorDTO for server errors")
    public void testHandleServerError() {
        Exception exception = new Exception("Server error");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn("/test");

        ErrorResponse response = exceptionHandler.handleServerError(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.status());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.name(), response.error());
        assertEquals("Unexpected error", response.message());
        assertEquals("/test", response.path());
    }

    @Test
    @DisplayName("Should handle ConflictException and return appropriate ErrorDTO")
    public void testHandleConflict() {
        ConflictException exception = new ConflictException("Conflict error");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn("/test");

        ErrorResponse response = exceptionHandler.handleConflict(exception, request);

        assertEquals(HttpStatus.CONFLICT.value(), response.status());
        assertEquals(HttpStatus.CONFLICT.name(), response.error());
        assertEquals("Conflict error", response.message());
        assertEquals("/test", response.path());
    }
}
