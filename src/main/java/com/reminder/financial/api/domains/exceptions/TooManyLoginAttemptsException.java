package com.reminder.financial.api.domains.exceptions;

import org.springframework.security.core.AuthenticationException;

public class TooManyLoginAttemptsException extends AuthenticationException {

    private final long retryAfterSeconds;

    public TooManyLoginAttemptsException(String message) {
        this(message, 0);
    }

    public TooManyLoginAttemptsException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
