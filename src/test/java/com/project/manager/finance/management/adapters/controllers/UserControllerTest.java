package com.project.manager.finance.management.adapters.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.manager.finance.management.adapters.dtos.user.CreateUserRequest;
import com.project.manager.finance.management.application.dtos.CreateUserCommand;
import com.project.manager.finance.management.application.dtos.ListUsersFilterQuery;
import com.project.manager.finance.management.application.dtos.ListUsersSingleResult;
import com.project.manager.finance.management.application.usecases.user.ListUsersUseCase;
import com.project.manager.finance.management.application.usecases.user.CreateUserUseCase;
import com.project.manager.finance.management.domains.models.user.enums.RoleEnum;
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

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateUserUseCase createUserUseCase;

    @MockBean
    private ListUsersUseCase listUsersUseCase;

    private CreateUserRequest validCreateUserDTO;

    @BeforeEach
    public void setup() {
        Mockito.reset(createUserUseCase, listUsersUseCase);

        validCreateUserDTO = new CreateUserRequest(
                "Test User",
                "test@example.com",
                "password123",
                RoleEnum.USER
        );
    }

    @Test
    @DisplayName("Should create a user successfully")
    public void testCreateUserSuccess() throws Exception {
        // Arrange
        Mockito.doNothing().when(createUserUseCase).execute(Mockito.any(CreateUserCommand.class));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateUserDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Should return Bad Request for invalid CreateDTO")
    public void testCreaterUserBadRequest() throws Exception {
        // Arrange
        CreateUserRequest invalidDTO = new CreateUserRequest(null, null, null, null);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Should list all users successfully with pagination")
    @WithMockUser(username = "admin", authorities = {"ADMIN_PERMISSION"})
    void testListUsersSuccess() throws Exception {
        // Arrange
        ListUsersSingleResult userResult = new ListUsersSingleResult("Test User", "test@example.com", RoleEnum.USER, true, false);
        Page<ListUsersSingleResult> resultPage = new PageImpl<>(List.of(userResult), PageRequest.of(0, 10), 1);

        when(listUsersUseCase.execute(any(ListUsersFilterQuery.class), any(Pageable.class))).thenReturn(resultPage);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/user/list")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Test User"))
                .andExpect(jsonPath("$.content[0].email").value("test@example.com"))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }


    @Test
    @DisplayName("Should return Forbidden for non-ADMIN role")
    @WithMockUser(username = "user", roles = {"USER"})
    public void testListUsersForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/user/list")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}