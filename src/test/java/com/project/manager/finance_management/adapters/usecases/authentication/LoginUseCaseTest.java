package com.project.manager.finance_management.adapters.usecases.authentication;

import com.project.manager.finance_management.adapters.dtos.authentication.AuthenticationDTO;
import com.project.manager.finance_management.adapters.dtos.authentication.LoginResponseDTO;
import com.project.manager.finance_management.domains.models.user.Role;
import com.project.manager.finance_management.domains.models.user.User;
import com.project.manager.finance_management.infrastructure.security.TokenProvider;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class LoginUseCaseTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private LoginUseCase loginUseCase;

    private AutoCloseable closeable;

    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    @DisplayName("Should return a token for valid login credentials")
    void execute() {
        AuthenticationDTO dto = new AuthenticationDTO("johndoe@example.com", "password");
        User user = new User("John Doe", "johndoe@example.com", "password", Role.USER);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, List.of());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(user)).thenReturn("validToken");

        LoginResponseDTO response = loginUseCase.execute(dto);

        assertNotNull(response);
        assertEquals("validToken", response.token());
    }

    @Test
    @DisplayName("Should throw an exception if authentication fails")
    void executeAuthenticationFailure() {
        AuthenticationDTO dto = new AuthenticationDTO("johndoe@example.com", "password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        assertThrows(RuntimeException.class, () -> loginUseCase.execute(dto));
    }
}
