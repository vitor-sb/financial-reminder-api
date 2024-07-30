package com.project.manager.finance_management.infrastructure.midlewares.exceptions;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
