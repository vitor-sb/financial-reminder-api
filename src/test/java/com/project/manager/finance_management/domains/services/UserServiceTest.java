package com.project.manager.finance_management.domains.services;

import com.project.manager.finance_management.domains.models.user.Role;
import com.project.manager.finance_management.domains.models.user.User;
import com.project.manager.finance_management.domains.repositories.jpa.UserRepository;
import com.project.manager.finance_management.infrastructure.midlewares.exceptions.ConflictException;
import com.project.manager.finance_management.infrastructure.midlewares.exceptions.NotFoundException;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService userService;

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
        User user = new User("John Doe", "johndoe@example.com", encodePassword("password"), Role.USER);
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
        User user = new User("John Doe", "johndoe@example.com", encodePassword("password"), Role.USER);
        when(repository.findUserByEmail("johndoe@example.com")).thenReturn(Optional.empty());
        when(repository.save(user)).thenReturn(user);

        User createdUser = userService.createUser(user);

        assertEquals("John Doe", createdUser.getName());
        assertEquals("johndoe@example.com", createdUser.getEmail());
        Assertions.assertTrue(new BCryptPasswordEncoder().matches("password", createdUser.getPassword()));
    }

    @Test
    @DisplayName("Should throw ConflictException if user already exists")
    void createUserThrowsConflictException() {
        User user = new User("John Doe", "johndoe@example.com", encodePassword("password"), Role.USER);
        when(repository.findUserByEmail("johndoe@example.com")).thenReturn(Optional.of(user));

        Exception exception = assertThrows(ConflictException.class, () -> userService.createUser(user));
        assertEquals("User with email johndoe@example.com already exists", exception.getMessage());
    }

    @Test
    @DisplayName("Should return user by email")
    void getUserByEmail() {
        User user = new User("John Doe", "johndoe@example.com", encodePassword("password"), Role.USER);
        when(repository.findUserByEmail("johndoe@example.com")).thenReturn(Optional.of(user));

        User foundUser = userService.getUserByEmail("johndoe@example.com");

        assertEquals("John Doe", foundUser.getName());
        assertEquals("johndoe@example.com", foundUser.getEmail());
    }

    @Test
    @DisplayName("Should throw NotFoundException if user not found")
    void getUserByEmailThrowsNotFoundException() {
        when(repository.findUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(NotFoundException.class, () -> userService.getUserByEmail("nonexistent@example.com"));
        assertEquals("User with email nonexistent@example.com not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should return list of all users")
    void listAllUsers() {
        User userOne = new User("John Doe", "johndoe@example.com", encodePassword("password"), Role.USER);
        User userTwo = new User("Jane Smith", "janesmith@example.com", encodePassword("password2"), Role.ADMIN);
        List<User> users = List.of(userOne, userTwo);

        when(repository.findAll()).thenReturn(users);

        List<User> result = userService.listAllUsers();

        assertEquals(2, result.size());
        assertTrue(result.contains(userTwo));
        assertTrue(result.contains(userTwo));
    }

    private static String encodePassword(String password) {
        return new BCryptPasswordEncoder().encode(password);
    }
}