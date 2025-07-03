package com.reminder.financial.api.application.usecases.user;

import com.reminder.financial.api.application.dtos.user.ListUsersFilterQuery;
import com.reminder.financial.api.application.dtos.user.ListUsersSingleResult;
import com.reminder.financial.api.application.usecases.user.impl.ListUsersUseCaseImpl;
import com.reminder.financial.api.domains.models.user.enums.RoleEnum;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.services.UserService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
        ListUsersFilterQuery filter = new ListUsersFilterQuery(
                null, null, RoleEnum.USER, null, null, null, null
        );
        Pageable pageable = PageRequest.of(0, 10);

        User user1 = new User("John Doe", "johndoe@example.com", "password", RoleEnum.USER);
        Page<User> userPage = new PageImpl<>(List.of(user1), pageable, 1);

        when(userService.listUsers(any(ListUsersFilterQuery.class), any(Pageable.class))).thenReturn(userPage);

        // Act
        Page<ListUsersSingleResult> resultPage = listUsersUseCase.execute(filter, pageable);

        // Assert
        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        ListUsersSingleResult resultDto = resultPage.getContent().get(0);
        assertEquals("John Doe", resultDto.name());
        assertEquals("johndoe@example.com", resultDto.email());
        assertEquals(RoleEnum.USER, resultDto.role());

        verify(userService, times(1)).listUsers(filter, pageable);
    }
}
