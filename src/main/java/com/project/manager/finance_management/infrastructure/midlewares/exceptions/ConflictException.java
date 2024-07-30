package com.project.manager.finance_management.infrastructure.midlewares.exceptions;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
