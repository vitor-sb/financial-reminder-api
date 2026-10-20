package com.reminder.financial.api.application.usecases.authentication.impl;

import com.reminder.financial.api.application.dtos.authentication.AuthenticationCommand;
import com.reminder.financial.api.application.dtos.authentication.AuthenticationResult;
import com.reminder.financial.api.application.usecases.authentication.LoginUseCase;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.application.exceptions.InvalidCredentialsException;
import com.reminder.financial.api.application.exceptions.TooManyLoginAttemptsException;
import com.reminder.financial.api.domains.services.LoginAttemptService;
import com.reminder.financial.api.infrastructure.security.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class LoginUseCaseImpl implements LoginUseCase {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private LoginAttemptService loginAttemptService;

    public AuthenticationResult execute(AuthenticationCommand dto) {
        String email = dto.email();

        if (loginAttemptService.isBlocked(email)) {
            long waitSeconds = TimeUnit.MILLISECONDS.toSeconds(loginAttemptService.getBlockUntil(email) - System.currentTimeMillis());
            throw new TooManyLoginAttemptsException("Too many login attempts. Try again later.", Math.max(waitSeconds, 1));
        }

        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(email, dto.password());
            var auth = authenticationManager.authenticate(usernamePassword);
            var token = tokenProvider.generateToken((User) auth.getPrincipal());

            loginAttemptService.resetAttempts(email);
            return new AuthenticationResult(token);

        } catch (AuthenticationException e) {
            loginAttemptService.incrementAttempts(email);
            if (e instanceof BadCredentialsException)
                throw new InvalidCredentialsException("Invalid email or password");
            throw e;
        }
    }
}