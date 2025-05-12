package com.project.manager.finance_management.infrastructure.midlewares.exceptions;

import org.springframework.security.authentication.BadCredentialsException;

public class InvalidCredentialsException extends BadCredentialsException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}