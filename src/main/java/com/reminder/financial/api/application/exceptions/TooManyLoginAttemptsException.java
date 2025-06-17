package com.reminder.financial.api.application.exceptions;

import org.springframework.security.core.AuthenticationException;

public class TooManyLoginAttemptsException extends AuthenticationException {
    public TooManyLoginAttemptsException(String message) {
        super(message);
    }
}
