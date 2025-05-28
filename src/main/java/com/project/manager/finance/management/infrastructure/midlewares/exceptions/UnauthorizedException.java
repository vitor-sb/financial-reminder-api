package com.project.manager.finance.management.infrastructure.midlewares.exceptions;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
