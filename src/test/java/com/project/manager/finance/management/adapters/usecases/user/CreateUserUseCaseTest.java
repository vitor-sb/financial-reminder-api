package com.project.manager.finance.management.adapters.usecases.user;

import com.project.manager.finance.management.adapters.dtos.user.CreateUserRequest;
import com.project.manager.finance.management.application.dtos.CreateUserCommand;
import com.project.manager.finance.management.application.usecases.user.CreateUserUseCase;
import com.project.manager.finance.management.domains.models.user.enums.RoleEnum;
import com.project.manager.finance.management.domains.models.user.User;
import com.project.manager.finance.management.domains.services.UserService;
import com.project.manager.finance.management.infrastructure.midlewares.exceptions.ConflictException;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
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
    void executeCreatesUser() {
        CreateUserCommand createUserDTO = new CreateUserCommand("John Doe", "johndoe@example.com", "password123", RoleEnum.USER);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        createUserUseCase.execute(createUserDTO);

        verify(userService, times(1)).createUser(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("John Doe", capturedUser.getName());
        assertEquals("johndoe@example.com", capturedUser.getEmail());
        assertEquals(RoleEnum.USER, capturedUser.getRole());
        assertTrue(passwordEncoder.matches("password123", capturedUser.getPassword()));
    }

    @Test
    @DisplayName("Should throw ConflictException if user already exists")
    void executeCreateUserThrowsConflictException() {
        CreateUserCommand createUserDTO = new CreateUserCommand("John Doe", "johndoe@example.com", "password123", RoleEnum.USER);
        doThrow(new ConflictException("Email already in use."))
                .when(userService)
                .checkEmailAvailability("johndoe@example.com");

        Exception exception = assertThrows(ConflictException.class, () -> createUserUseCase.execute(createUserDTO));

        assertEquals("Email already in use.", exception.getMessage());
        verify(userService).checkEmailAvailability("johndoe@example.com");
    }
}
