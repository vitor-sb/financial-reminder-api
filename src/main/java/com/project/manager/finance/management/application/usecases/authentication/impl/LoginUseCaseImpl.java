package com.project.manager.finance.management.application.usecases.authentication.impl;

import com.project.manager.finance.management.application.dtos.AuthenticationCommand;
import com.project.manager.finance.management.application.dtos.AuthenticationResult;
import com.project.manager.finance.management.application.usecases.authentication.LoginUseCase;
import com.project.manager.finance.management.domains.models.user.User;
import com.project.manager.finance.management.infrastructure.midlewares.exceptions.InvalidCredentialsException;
import com.project.manager.finance.management.infrastructure.midlewares.exceptions.TooManyLoginAttemptsException;
import com.project.manager.finance.management.domains.services.LoginAttemptService;
import com.project.manager.finance.management.infrastructure.security.TokenProvider;
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
            long waitTime = loginAttemptService.getBlockUntil(email) - System.currentTimeMillis();
            throw new TooManyLoginAttemptsException("Account blocked. Try again in " + TimeUnit.MILLISECONDS.toSeconds(waitTime) + " seconds.");
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