package com.project.manager.finance.management.adapters.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.manager.finance.management.adapters.dtos.user.CreateUserRequest;
import com.project.manager.finance.management.adapters.dtos.user.ListUsersSingleResponse;
import com.project.manager.finance.management.application.dtos.CreateUserCommand;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;
import java.util.List;

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
    @DisplayName("Should list all users successfully for ADMIN_PERMISSION")
    @WithMockUser(username = "admin", authorities = {"ADMIN_PERMISSION"})
    public void testListUsersSuccess() throws Exception {
        // Arrange
        List<ListUsersSingleResult> usersList = Collections.singletonList(
                new ListUsersSingleResult("Test User", "test@example.com", RoleEnum.USER, true, false)
        );
        Mockito.when(listUsersUseCase.execute()).thenReturn(usersList);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/user/list")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Test User"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].email").value("test@example.com"));
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