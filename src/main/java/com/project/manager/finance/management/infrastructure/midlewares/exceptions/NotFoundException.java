package com.project.manager.finance.management.infrastructure.midlewares.exceptions;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
