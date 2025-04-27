package com.project.manager.finance_management.infrastructure.midlewares.exceptions;

public class RedisConnectionException extends RuntimeException {
    public RedisConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
