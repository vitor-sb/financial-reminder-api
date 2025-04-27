package com.project.manager.finance_management.adapters.usecases.user;

import com.project.manager.finance_management.adapters.dtos.user.ListAllUsersResponseDTO;
import com.project.manager.finance_management.domains.models.user.Role;
import com.project.manager.finance_management.domains.models.user.User;
import com.project.manager.finance_management.domains.services.UserService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ListAllUsersUseCaseTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private ListAllUsersUseCase listAllUsersUseCase;

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
    @DisplayName("Should return list of ListAllUsersResponseDTO")
    void executeReturnsListOfListAllUsersResponseDTO() {
        // Arrange
        User user1 = new User("John Doe", "johndoe@example.com", "password1", Role.USER);
        User user2 = new User("Jane Smith", "janesmith@example.com", "password2", Role.ADMIN);
        List<User> users = List.of(user1, user2);

        when(userService.listAllUsers()).thenReturn(users);

        List<ListAllUsersResponseDTO> response = listAllUsersUseCase.execute();

        assertNotNull(response);
        assertEquals(2, response.size());

        ListAllUsersResponseDTO dto1 = response.get(0);
        assertEquals("John Doe", dto1.name());
        assertEquals("johndoe@example.com", dto1.email());
        assertEquals(Role.USER, dto1.role());

        ListAllUsersResponseDTO dto2 = response.get(1);
        assertEquals("Jane Smith", dto2.name());
        assertEquals("janesmith@example.com", dto2.email());
        assertEquals(Role.ADMIN, dto2.role());

        verify(userService, times(1)).listAllUsers();
    }
}
