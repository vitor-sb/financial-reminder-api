package com.project.manager.finance_management.infrastructure.midlewares.exceptions;

public class SecretKeyNotSetException extends RuntimeException {
    public SecretKeyNotSetException(String message) {
        super(message);
    }
}
