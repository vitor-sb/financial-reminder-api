package com.project.manager.finance.management.infrastructure.midlewares.exceptions;

public class RedisConnectionException extends RuntimeException {
    public RedisConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
