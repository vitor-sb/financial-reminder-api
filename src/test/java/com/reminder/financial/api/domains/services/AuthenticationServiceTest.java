package com.reminder.financial.api.domains.services;

import com.reminder.financial.api.domains.models.user.enums.RoleEnum;
import com.reminder.financial.api.domains.models.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class AuthenticationServiceTest {
    @Mock
    private UserService userService;

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
        User user = new User("John Doe", "johndoe@example.com", "password", RoleEnum.USER);
        when(userService.findValidUserByEmail("johndoe@example.com")).thenReturn(user);

        UserDetails userDetails = authenticationService.loadUserByUsername("johndoe@example.com");

        assertEquals("johndoe@example.com", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("USER_PERMISSION")));
    }
}