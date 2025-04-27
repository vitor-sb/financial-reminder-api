package com.project.manager.finance_management.infrastructure.midlewares.exceptions;

import org.springframework.security.core.AuthenticationException;

public class InvalidCredentialsException extends AuthenticationException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}