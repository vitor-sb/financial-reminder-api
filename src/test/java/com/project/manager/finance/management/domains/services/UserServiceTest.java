package com.project.manager.finance.management.domains.services;

import com.project.manager.finance.management.domains.models.user.enums.RoleEnum;
import com.project.manager.finance.management.domains.models.user.User;
import com.project.manager.finance.management.domains.repositories.jpa.UserRepository;
import com.project.manager.finance.management.infrastructure.midlewares.exceptions.NotFoundException;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {
    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    @DisplayName("Should return true when user exists")
    void userExistsCaseOne() {
        User user = new User("John Doe", "johndoe@example.com", passwordEncoder.encode("password"), RoleEnum.USER);
        when(repository.findUserByEmail("johndoe@example.com")).thenReturn(Optional.of(user));
        assertTrue(userService.userExists("johndoe@example.com"));
    }

    @Test
    @DisplayName("Should return false when user does not exist")
    void userExistsCaseTwo() {
        when(repository.findUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        assertFalse(userService.userExists("nonexistent@example.com"));
    }

    @Test
    @DisplayName("Should create user if it does not exist")
    void createUser() {
        User user = new User("John Doe", "johndoe@example.com", passwordEncoder.encode("password"), RoleEnum.USER);
        when(repository.findUserByEmail("johndoe@example.com")).thenReturn(Optional.empty());
        when(repository.save(user)).thenReturn(user);

        User createdUser = userService.createUser(user);

        assertEquals("John Doe", createdUser.getName());
        assertEquals("johndoe@example.com", createdUser.getEmail());
        Assertions.assertTrue(new BCryptPasswordEncoder().matches("password", createdUser.getPassword()));
    }

    @Test
    @DisplayName("Should return user by email")
    void getUserByEmail() {
        User user = new User("John Doe", "johndoe@example.com", passwordEncoder.encode("password"), RoleEnum.USER);
        when(repository.findUserByEmail("johndoe@example.com")).thenReturn(Optional.of(user));

        User foundUser = userService.getUserByEmail("johndoe@example.com").orElseThrow(
                () -> new NotFoundException("User not found")
        );

        assertEquals("John Doe", foundUser.getName());
        assertEquals("johndoe@example.com", foundUser.getEmail());
    }

    @Test
    @DisplayName("Should throw NotFoundException if user not found")
    void getUserByEmailThrowsNotFoundException() {
        when(repository.findUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Optional<User> foundUser = userService.getUserByEmail("nonexistent@example.com");
        assertTrue(foundUser.isEmpty());
    }

    @Test
    @DisplayName("Should return list of users")
    void listAllUsers() {
        User userOne = new User("John Doe", "johndoe@example.com", passwordEncoder.encode("password"), RoleEnum.USER);
        User userTwo = new User("Jane Smith", "janesmith@example.com", passwordEncoder.encode("password2"), RoleEnum.ADMIN);
        List<User> users = List.of(userOne, userTwo);

        when(repository.findAll()).thenReturn(users);

        List<User> result = userService.listUsers();

        assertEquals(2, result.size());
        assertTrue(result.contains(userTwo));
        assertTrue(result.contains(userTwo));
    }
}