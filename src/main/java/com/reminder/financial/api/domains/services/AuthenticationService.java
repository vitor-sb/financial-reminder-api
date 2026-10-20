package com.reminder.financial.api.domains.services;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthenticationService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) {
        // inexistente, desativado e excluído são indistinguíveis para quem chama
        try {
            return userService.findValidUserByEmail(username);
        } catch (RuntimeException e) {
            throw new UsernameNotFoundException("Invalid email or password");
        }
    }
}
