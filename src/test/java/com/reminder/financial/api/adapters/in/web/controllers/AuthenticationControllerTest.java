package com.reminder.financial.api.adapters.in.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reminder.financial.api.adapters.in.web.dtos.authentication.AuthenticationRequest;
import com.reminder.financial.api.application.dtos.authentication.AuthenticationCommand;
import com.reminder.financial.api.application.dtos.authentication.AuthenticationResult;
import com.reminder.financial.api.application.usecases.authentication.LoginUseCase;
import com.reminder.financial.api.domains.exceptions.InvalidCredentialsException;
import com.reminder.financial.api.domains.exceptions.TooManyLoginAttemptsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoginUseCase loginUseCase;

    @MockBean
    private com.reminder.financial.api.application.security.LoginAttemptService loginAttemptService;

    private String validUserEmail = "test@example.com";
    private String validUserPassword = "password123";

    @BeforeEach
    public void setup() {
        Mockito.reset(loginUseCase);
    }

    @Test
    @DisplayName("Should return a token on successful login")
    public void testLoginSuccess() throws Exception {
        // Arrange
        AuthenticationRequest dto = new AuthenticationRequest(validUserEmail, validUserPassword);
        AuthenticationResult result = new AuthenticationResult("mockedToken");
        Mockito.when(loginUseCase.execute(Mockito.any(AuthenticationCommand.class))).thenReturn(result);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").value("mockedToken"));
    }

    @Test
    @DisplayName("Should throw an InvalidCredentialsException if authentication fails")
    public void testLoginInvalidCredentials() throws Exception {
        // Arrange
        AuthenticationRequest dto = new AuthenticationRequest("invalid@example.com", "invalidPassword");
        Mockito.when(loginUseCase.execute(Mockito.any(AuthenticationCommand.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().is(401))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("Should throw an TooManyLoginAttemptsException if the account is blocked")
    public void testLoginAccountBlocked() throws Exception {
        // Arrange
        AuthenticationRequest dto = new AuthenticationRequest(validUserEmail, validUserPassword);
        Mockito.when(loginUseCase.execute(Mockito.any(AuthenticationCommand.class)))
                .thenThrow(new TooManyLoginAttemptsException("Account blocked. Try again later"));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().is(429))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Account blocked. Try again later"));
    }

    @Test
    @DisplayName("Should return Retry-After header when the login is blocked")
    public void testLoginBlockedHasRetryAfter() throws Exception {
        AuthenticationRequest dto = new AuthenticationRequest(validUserEmail, validUserPassword);
        Mockito.when(loginUseCase.execute(Mockito.any(AuthenticationCommand.class)))
                .thenThrow(new TooManyLoginAttemptsException("Too many login attempts. Try again later.", 120));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().is(429))
                .andExpect(MockMvcResultMatchers.header().string("Retry-After", "120"));
    }

    @Test
    @DisplayName("Should return Bad Request if email is missing")
    public void testLoginWithMissingEmail() throws Exception {
        // Arrange
        AuthenticationRequest dto = new AuthenticationRequest(null, validUserPassword);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().is(400));
    }

    @Test
    @DisplayName("Should return Bad Request if password is missing")
    public void testLoginWithMissingPassword() throws Exception {
        // Arrange
        AuthenticationRequest dto = new AuthenticationRequest(validUserEmail, null);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().is(400));
    }
}