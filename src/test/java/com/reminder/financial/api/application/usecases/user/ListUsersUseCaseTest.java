package com.reminder.financial.api.application.usecases.user;

import com.reminder.financial.api.domains.filters.UserFilter;
import com.reminder.financial.api.application.dtos.user.ListUsersSingleResult;
import com.reminder.financial.api.application.usecases.user.impl.ListUsersUseCaseImpl;
import com.reminder.financial.api.domains.models.user.enums.RoleEnum;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.services.UserService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import com.reminder.financial.api.domains.pagination.PageRequest;
import com.reminder.financial.api.domains.pagination.PageResult;

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
    @DisplayName("Should return page of ListUsersSingleResult")
    void execute_shouldReturnPageOfListUsersSingleResult() {
        // Arrange
        UserFilter filter = new UserFilter(
                null, null, RoleEnum.USER, null, null, null, null
        );
        PageRequest pageable = PageRequest.of(0, 10);

        User user1 = new User("John Doe", "johndoe@example.com", "password", RoleEnum.USER);
        PageResult<User> userPage = new PageResult<>(List.of(user1), pageable, 1);

        when(userService.listUsers(any(UserFilter.class), any(PageRequest.class))).thenReturn(userPage);

        // Act
        PageResult<ListUsersSingleResult> resultPage = listUsersUseCase.execute(filter, pageable);

        // Assert
        assertNotNull(resultPage);
        assertEquals(1, resultPage.totalElements());
        ListUsersSingleResult resultDto = resultPage.content().get(0);
        assertEquals("John Doe", resultDto.name());
        assertEquals("johndoe@example.com", resultDto.email());
        assertEquals(RoleEnum.USER, resultDto.role());

        verify(userService, times(1)).listUsers(filter, pageable);
    }
}
