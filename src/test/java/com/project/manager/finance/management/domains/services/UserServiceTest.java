package com.project.manager.finance.management.domains.services;

import com.project.manager.finance.management.application.dtos.user.ListUsersFilterQuery;
import com.project.manager.finance.management.domains.models.user.enums.RoleEnum;
import com.project.manager.finance.management.domains.models.user.User;
import com.project.manager.finance.management.domains.repositories.jpa.UserRepository;
import com.project.manager.finance.management.application.exceptions.NotFoundException;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    @DisplayName("Should create user if it does not exist")
    void save() {
        User user = new User("John Doe", "johndoe@example.com", passwordEncoder.encode("password"), RoleEnum.USER);
        when(repository.findUserByEmail("johndoe@example.com")).thenReturn(Optional.empty());
        when(repository.save(user)).thenReturn(user);

        User createdUser = userService.save(user);

        assertEquals("John Doe", createdUser.getName());
        assertEquals("johndoe@example.com", createdUser.getEmail());
        Assertions.assertTrue(new BCryptPasswordEncoder().matches("password", createdUser.getPassword()));
    }

    @Test
    @DisplayName("Should return user by email")
    void getUserByEmail() {
        User user = new User("John Doe", "johndoe@example.com", passwordEncoder.encode("password"), RoleEnum.USER);
        when(repository.findUserByEmailAndAuditEnabledTrueAndAuditDeletedFalse("johndoe@example.com")).thenReturn(Optional.of(user));

        User foundUser = userService.getActiveUserByEmail("johndoe@example.com").orElseThrow(
                () -> new NotFoundException("User not found")
        );

        assertEquals("John Doe", foundUser.getName());
        assertEquals("johndoe@example.com", foundUser.getEmail());
    }

    @Test
    @DisplayName("Should throw NotFoundException if user not found")
    void getUserByEmailThrowsNotFoundException() {
        when(repository.findUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Optional<User> foundUser = userService.getActiveUserByEmail("nonexistent@example.com");
        assertTrue(foundUser.isEmpty());
    }

    @Test
    @DisplayName("Should return a page of users")
    void listUsersReturnsPageOfUsers() {
        // Arrange
        ListUsersFilterQuery filter = new ListUsersFilterQuery(null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        User userOne = new User("John Doe", "johndoe@example.com", "password", RoleEnum.USER);
        User userTwo = new User("Jane Smith", "janesmith@example.com", "password2", RoleEnum.ADMIN);
        List<User> users = List.of(userOne, userTwo);
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());

        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        // Act
        Page<User> result = userService.listUsers(filter, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals("John Doe", result.getContent().get(0).getName());

        verify(repository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }
}