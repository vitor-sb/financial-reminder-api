package com.project.manager.finance_management.domains.services;

import com.project.manager.finance_management.domains.repositories.UserRepository;
import com.project.manager.finance_management.infrastructure.midlewares.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService implements UserDetailsService {
    @Autowired
    UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return repository.findUserByEmail(username).orElseThrow(
                () -> new NotFoundException("User with email " + username + " not found")
        );
    }
}
