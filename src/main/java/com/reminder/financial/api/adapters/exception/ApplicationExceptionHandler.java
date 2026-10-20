package com.reminder.financial.api.adapters.exception;

import com.reminder.financial.api.application.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException exception, HttpServletRequest request) {
        return new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.name(),
                exception.getMessage(),
                request.getServletPath()
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUnauthorized(UnauthorizedException exception, HttpServletRequest request) {
        return new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.name(),
                exception.getMessage(),
                request.getServletPath()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationError(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> errorMessage = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(e -> errorMessage.put(e.getField(), e.getDefaultMessage()));
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                errorMessage.toString(),
                request.getServletPath()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleServerError(Exception exception, HttpServletRequest request) {
        return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                exception.getMessage(),
                request.getServletPath()
        );
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(ConflictException exception, HttpServletRequest request) {
        return new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.name(),
                exception.getMessage(),
                request.getServletPath()
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthenticationException(AuthenticationException exception, HttpServletRequest request) {
        return new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.name(),
                "Invalid email or password",
                request.getServletPath()
        );
    }

    @ExceptionHandler(TooManyLoginAttemptsException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ErrorResponse handleTooManyLoginAttemptsException(TooManyLoginAttemptsException exception, HttpServletRequest request, HttpServletResponse response) {
        if (exception.getRetryAfterSeconds() > 0) {
            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(exception.getRetryAfterSeconds()));
        }
        return new ErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                HttpStatus.TOO_MANY_REQUESTS.name(),
                exception.getMessage(),
                request.getServletPath()
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidCredentialsException(InvalidCredentialsException exception, HttpServletRequest request) {
        return new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.name(),
                exception.getMessage(),
                request.getServletPath()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDeniedException(AccessDeniedException exception, HttpServletRequest request) {
        return new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.name(),
                exception.getMessage(),
                request.getServletPath()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException exception, HttpServletRequest request) {
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                exception.getMessage(),
                request.getServletPath()
        );
    }
}
