package com.reminder.financial.api.adapters.controllers;

import com.reminder.financial.api.application.dtos.user.ListUsersFilterQuery;
import com.reminder.financial.api.application.dtos.user.ListUsersSingleResult;
import com.reminder.financial.api.application.usecases.user.ListUsersUseCase;
import com.reminder.financial.api.domains.models.user.enums.RoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListUsersUseCase listUsersUseCase;


    @BeforeEach
    public void setup() {
        Mockito.reset(listUsersUseCase);
    }

    @Test
    @DisplayName("Should list all users successfully with pagination")
    @WithMockUser(username = "admin", authorities = {"ADMIN_PERMISSION"})
    void testListUsersSuccess() throws Exception {
        // Arrange
        ListUsersSingleResult userResult = new ListUsersSingleResult(
                "Test User", "test@example.com", RoleEnum.USER, null, true, false
        );
        Page<ListUsersSingleResult> resultPage = new PageImpl<>(
                List.of(userResult), PageRequest.of(0, 10), 1
        );

        when(listUsersUseCase.execute(any(ListUsersFilterQuery.class), any(Pageable.class))).thenReturn(resultPage);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/users/list")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Test User"))
                .andExpect(jsonPath("$.content[0].email").value("test@example.com"))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }


    @Test
    @DisplayName("Should return Forbidden for non-ADMIN role")
    @WithMockUser(username = "user", roles = {"USER"})
    public void testListUsersForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/users/list")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}