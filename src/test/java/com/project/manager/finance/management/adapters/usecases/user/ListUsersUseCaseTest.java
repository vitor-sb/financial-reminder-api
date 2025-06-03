package com.project.manager.finance.management.adapters.usecases.user;

import com.project.manager.finance.management.application.dtos.ListUsersSingleResult;
import com.project.manager.finance.management.application.usecases.user.ListUsersUseCase;
import com.project.manager.finance.management.application.usecases.user.impl.ListUsersUseCaseImpl;
import com.project.manager.finance.management.domains.models.user.enums.RoleEnum;
import com.project.manager.finance.management.domains.models.user.User;
import com.project.manager.finance.management.domains.services.UserService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ListUsersUseCaseTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private ListUsersUseCaseImpl listUsersUseCase;

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
    @DisplayName("Should return list of ListUsersResponseDTO")
    void executeReturnsListOfListUsersResponseDTO() {
        // Arrange
        User user1 = new User("John Doe", "johndoe@example.com", "password1", RoleEnum.USER);
        User user2 = new User("Jane Smith", "janesmith@example.com", "password2", RoleEnum.ADMIN);
        List<User> users = List.of(user1, user2);

        when(userService.listUsers()).thenReturn(users);

        List<ListUsersSingleResult> response = listUsersUseCase.execute();

        assertNotNull(response);
        assertEquals(2, response.size());

        ListUsersSingleResult dto1 = response.get(0);
        assertEquals("John Doe", dto1.name());
        assertEquals("johndoe@example.com", dto1.email());
        assertEquals(RoleEnum.USER, dto1.role());

        ListUsersSingleResult dto2 = response.get(1);
        assertEquals("Jane Smith", dto2.name());
        assertEquals("janesmith@example.com", dto2.email());
        assertEquals(RoleEnum.ADMIN, dto2.role());

        verify(userService, times(1)).listUsers();
    }
}
