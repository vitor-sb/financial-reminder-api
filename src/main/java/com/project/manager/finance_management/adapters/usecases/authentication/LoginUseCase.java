package com.project.manager.finance_management.adapters.usecases.authentication;

import com.project.manager.finance_management.adapters.dtos.authentication.AuthenticationDTO;
import com.project.manager.finance_management.adapters.dtos.authentication.LoginResponseDTO;
import com.project.manager.finance_management.domains.models.user.User;
import com.project.manager.finance_management.infrastructure.midlewares.exceptions.InvalidCredentialsException;
import com.project.manager.finance_management.infrastructure.midlewares.exceptions.TooManyLoginAttemptsException;
import com.project.manager.finance_management.domains.services.LoginAttemptService;
import com.project.manager.finance_management.infrastructure.security.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class LoginUseCase {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private LoginAttemptService loginAttemptService;


    public LoginResponseDTO execute(AuthenticationDTO dto) {
        String email = dto.email();

        if (loginAttemptService.isBlocked(email)) {
            long waitTime = loginAttemptService.getBlockUntil(email) - System.currentTimeMillis();
            throw new TooManyLoginAttemptsException("Account blocked. Try again in " + TimeUnit.MILLISECONDS.toSeconds(waitTime) + " seconds.");
        }

        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(email, dto.password());
            var auth = this.authenticationManager.authenticate(usernamePassword);
            var token = this.tokenProvider.generateToken((User) auth.getPrincipal());

            loginAttemptService.resetAttempts(email);
            return new LoginResponseDTO(token);

        } catch (AuthenticationException e) {
            handleFailedLogin(email, e);
            return null;
        }
    }

    private void handleFailedLogin(String email, AuthenticationException e) {
        loginAttemptService.incrementAttempts(email);
        if (loginAttemptService.isBlocked(email)) {
            long waitTime = loginAttemptService.getBlockUntil(email) - System.currentTimeMillis();
            throw new TooManyLoginAttemptsException("Account blocked. Try again in " + TimeUnit.MILLISECONDS.toSeconds(waitTime) + " seconds.");
        } else {
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }
}