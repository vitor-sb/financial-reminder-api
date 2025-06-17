package com.reminder.financial.api.application.usecases.authentication;

import com.reminder.financial.api.application.dtos.authentication.AuthenticationCommand;
import com.reminder.financial.api.application.dtos.authentication.AuthenticationResult;
import com.reminder.financial.api.application.usecases.authentication.impl.LoginUseCaseImpl;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.application.exceptions.InvalidCredentialsException;
import com.reminder.financial.api.application.exceptions.TooManyLoginAttemptsException;
import com.reminder.financial.api.domains.services.LoginAttemptService;
import com.reminder.financial.api.infrastructure.security.TokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginUseCaseTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private LoginUseCaseImpl loginUseCase;

    private AutoCloseable closeable;

    private final String validEmail = "test@example.com";
    private final String validPassword = "password123";
    private final String invalidEmail = "invalid@example.com";
    private final String invalidPassword = "invalidPassword";

    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    @DisplayName("Should return a LoginResponseDTO on successful login")
    void testExecuteSuccessfulLogin() {
        // Arrange
        AuthenticationCommand dto = new AuthenticationCommand(validEmail, validPassword);
        org.springframework.security.core.Authentication authResult = mock(org.springframework.security.core.Authentication.class);
        User user = new User();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authResult);
        when(authResult.getPrincipal()).thenReturn(user);
        when(tokenProvider.generateToken(user)).thenReturn("mockedToken");

        // Act
        AuthenticationResult response = loginUseCase.execute(dto);

        // Assert
        assertNotNull(response);
        assertEquals("mockedToken", response.token());
        verify(loginAttemptService, times(1)).resetAttempts(validEmail);
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException for invalid credentials and handle login attempts")
    void testExecuteInvalidCredentialsAndHandleAttempts() {
        // Arrange
        AuthenticationCommand dto = new AuthenticationCommand(invalidEmail, invalidPassword);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials") {});

        when(loginAttemptService.isBlocked(invalidEmail)).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> loginUseCase.execute(dto));
        verify(loginAttemptService, times(1)).incrementAttempts(invalidEmail);
        verify(loginAttemptService, never()).resetAttempts(anyString());
    }

    @Test
    @DisplayName("Should throw TooManyLoginAttemptsException when account is blocked")
    void testExecute_AccountBlocked() {
        // Arrange
        AuthenticationCommand dto = new AuthenticationCommand(validEmail, validPassword);
        when(loginAttemptService.isBlocked(validEmail)).thenReturn(true);
        when(loginAttemptService.getBlockUntil(validEmail)).thenReturn(System.currentTimeMillis() + 60000);

        // Act & Assert
        assertThrows(TooManyLoginAttemptsException.class, () -> loginUseCase.execute(dto));
        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}