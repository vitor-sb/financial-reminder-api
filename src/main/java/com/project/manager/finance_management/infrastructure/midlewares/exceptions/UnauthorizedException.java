package com.project.manager.finance_management.infrastructure.midlewares.exceptions;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
