package com.reminder.financial.api.adapters.out.security;

import com.reminder.financial.api.application.security.CredentialsAuthenticator;
import com.reminder.financial.api.domains.exceptions.InvalidCredentialsException;
import com.reminder.financial.api.domains.models.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringCredentialsAuthenticator implements CredentialsAuthenticator {

    private final AuthenticationManager authenticationManager;

    @Override
    public User authenticate(String email, String password) {
        try {
            var auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            return (User) auth.getPrincipal();
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }
}
