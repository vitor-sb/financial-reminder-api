package com.project.manager.finance.management.application.exceptions;

import org.springframework.security.core.AuthenticationException;

public class TooManyLoginAttemptsException extends AuthenticationException {
    public TooManyLoginAttemptsException(String message) {
        super(message);
    }
}
