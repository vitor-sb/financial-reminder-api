package com.project.manager.finance_management.domains.services;

import com.project.manager.finance_management.domains.models.user.Role;
import com.project.manager.finance_management.domains.models.user.User;
import com.project.manager.finance_management.domains.repositories.UserRepository;
import com.project.manager.finance_management.infrastructure.midlewares.exceptions.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class AuthenticationServiceTest {
    @Mock
    private UserRepository repository;

    @InjectMocks
    private AuthenticationService authenticationService;

    private AutoCloseable closeable;

    @BeforeEach
    void setup() {
        this.closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        this.closeable.close();
    }

    @Test
    @DisplayName("Should return UserDetails when user is found")
    void loadUserByUsername() {
        User user = new User("John Doe", "johndoe@example.com", "password", Role.USER);
        when(repository.findByLogin("johndoe@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = authenticationService.loadUserByUsername("johndoe@example.com");

        assertEquals("johndoe@example.com", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("Should throw NotFoundException when user is not found")
    void loadUserByUsernameThrowsNotFoundException() {
        when(repository.findByLogin("nonexistent@example.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(NotFoundException.class, () -> authenticationService.loadUserByUsername("nonexistent@example.com"));
        assertEquals("User with login nonexistent@example.com not found", exception.getMessage());
    }}
