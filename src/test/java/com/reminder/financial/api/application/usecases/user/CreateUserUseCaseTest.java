package com.reminder.financial.api.application.usecases.user;

import com.reminder.financial.api.application.dtos.user.CreateUserCommand;
import com.reminder.financial.api.domains.models.user.enums.RoleEnum;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.services.UserService;
import com.reminder.financial.api.application.exceptions.ConflictException;
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
        CreateUserCommand createUserDTO = new CreateUserCommand("John Doe", "johndoe@example.com", "password123");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        createUserUseCase.execute(createUserDTO);

        verify(userService, times(1)).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("John Doe", capturedUser.getName());
        assertEquals("johndoe@example.com", capturedUser.getEmail());
        assertEquals(RoleEnum.USER, capturedUser.getRole());
        assertTrue(passwordEncoder.matches("password123", capturedUser.getPassword()));
    }

    @Test
    @DisplayName("Should throw ConflictException if user already exists")
    void executeCreateUserThrowsConflictException() {
        CreateUserCommand createUserDTO = new CreateUserCommand("John Doe", "johndoe@example.com", "password123");
        doThrow(new ConflictException("Email already in use."))
                .when(userService)
                .checkEmailAvailability("johndoe@example.com");

        Exception exception = assertThrows(ConflictException.class, () -> createUserUseCase.execute(createUserDTO));

        assertEquals("Email already in use.", exception.getMessage());
        verify(userService).checkEmailAvailability("johndoe@example.com");
    }
}
