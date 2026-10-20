package com.reminder.financial.api.application.usecases.authentication.impl;

import com.reminder.financial.api.application.dtos.authentication.AuthenticationCommand;
import com.reminder.financial.api.application.dtos.authentication.AuthenticationResult;
import com.reminder.financial.api.application.usecases.authentication.LoginUseCase;
import com.reminder.financial.api.domains.exceptions.InvalidCredentialsException;
import com.reminder.financial.api.domains.exceptions.TooManyLoginAttemptsException;
import com.reminder.financial.api.application.security.LoginAttemptService;
import com.reminder.financial.api.application.security.CredentialsAuthenticator;
import com.reminder.financial.api.application.security.TokenIssuer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class LoginUseCaseImpl implements LoginUseCase {

    private static final Logger logger = LoggerFactory.getLogger(LoginUseCaseImpl.class);

    private final CredentialsAuthenticator authenticator;

    private final TokenIssuer tokenIssuer;

    private final LoginAttemptService loginAttemptService;

    public AuthenticationResult execute(AuthenticationCommand dto) {
        String email = dto.email();

        if (loginAttemptService.isBlocked(email)) {
            logger.warn("login blocked reason=too_many_attempts");
            long waitSeconds = TimeUnit.MILLISECONDS.toSeconds(loginAttemptService.getBlockUntil(email) - System.currentTimeMillis());
            throw new TooManyLoginAttemptsException("Too many login attempts. Try again later.", Math.max(waitSeconds, 1));
        }

        try {
            var token = tokenIssuer.issue(authenticator.authenticate(email, dto.password()));

            loginAttemptService.resetAttempts(email);
            return new AuthenticationResult(token);

        } catch (InvalidCredentialsException e) {
            loginAttemptService.incrementAttempts(email);
            logger.warn("login failed reason=invalid_credentials");
            throw e;
        }
    }
}