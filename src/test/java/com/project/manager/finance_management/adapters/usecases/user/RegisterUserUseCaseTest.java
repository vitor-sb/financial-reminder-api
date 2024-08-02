package com.project.manager.finance_management.adapters.usecases.user;

import com.project.manager.finance_management.adapters.dtos.user.RegisterDTO;
import com.project.manager.finance_management.domains.models.user.Role;
import com.project.manager.finance_management.domains.models.user.User;
import com.project.manager.finance_management.domains.services.UserService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class RegisterUserUseCaseTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private RegisterUserUseCase registerUserUseCase;

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
    @DisplayName("Should create user with encrypted password and valid email")
    void executeCreatesUserWithEncryptedPassword() {
        RegisterDTO registerDTO = new RegisterDTO("John Doe", "johndoe@example.com", "password123", Role.USER);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        registerUserUseCase.execute(registerDTO);

        verify(userService, times(1)).createUser(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("John Doe", capturedUser.getName());
        assertEquals("johndoe@example.com", capturedUser.getEmail());
        assertEquals(1, capturedUser.getRoles().size());
        assertEquals(Role.USER, capturedUser.getRoles().get(0));
        assertTrue(new BCryptPasswordEncoder().matches("password123", capturedUser.getPassword()));
    }
}
