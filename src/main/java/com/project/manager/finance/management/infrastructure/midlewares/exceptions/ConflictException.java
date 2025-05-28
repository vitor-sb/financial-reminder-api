package com.project.manager.finance.management.infrastructure.midlewares.exceptions;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
