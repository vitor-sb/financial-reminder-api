package com.project.manager.finance_management.adapters.usecases.user;

import com.project.manager.finance_management.adapters.dtos.user.CreateUserDTO;
import com.project.manager.finance_management.domains.models.user.Role;
import com.project.manager.finance_management.domains.models.user.User;
import com.project.manager.finance_management.domains.services.UserService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class CreateUserUseCaseTest {

    @MockBean
    private UserService userService;

    @Autowired
    private CreateUserUseCase createUserUseCase;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        CreateUserDTO createUserDTO = new CreateUserDTO("John Doe", "johndoe@example.com", "password123", Role.USER);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        createUserUseCase.execute(createUserDTO);

        verify(userService, times(1)).createUser(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("John Doe", capturedUser.getName());
        assertEquals("johndoe@example.com", capturedUser.getEmail());
        assertEquals(Role.USER, capturedUser.getRole());
        assertTrue(passwordEncoder.matches("password123", capturedUser.getPassword()));
    }
}
