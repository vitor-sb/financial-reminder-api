package com.project.manager.finance_management.adapters.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.manager.finance_management.adapters.dtos.user.ListAllUsersResponseDTO;
import com.project.manager.finance_management.adapters.dtos.user.RegisterDTO;
import com.project.manager.finance_management.adapters.usecases.user.ListAllUsersUseCase;
import com.project.manager.finance_management.adapters.usecases.user.RegisterUserUseCase;
import com.project.manager.finance_management.domains.models.user.Role;
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
    private RegisterUserUseCase registerUserUseCase;

    @MockBean
    private ListAllUsersUseCase listAllUsersUseCase;

    private RegisterDTO validRegisterDTO;

    @BeforeEach
    public void setup() {
        Mockito.reset(registerUserUseCase, listAllUsersUseCase);

        validRegisterDTO = new RegisterDTO(
                "Test User",
                "test@example.com",
                "password123",
                Role.USER
        );
    }

    @Test
    @DisplayName("Should register a user successfully")
    public void testRegisterUserSuccess() throws Exception {
        // Arrange
        Mockito.doNothing().when(registerUserUseCase).execute(Mockito.any(RegisterDTO.class));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Should return Bad Request for invalid RegisterDTO")
    public void testRegisterUserBadRequest() throws Exception {
        // Arrange
        RegisterDTO invalidDTO = new RegisterDTO(null, null, null, null);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Should list all users successfully for ADMIN_PERMISSION")
    @WithMockUser(username = "admin", authorities = {"ADMIN_PERMISSION"})
    public void testListAllUsersSuccess() throws Exception {
        // Arrange
        List<ListAllUsersResponseDTO> usersList = Collections.singletonList(
                new ListAllUsersResponseDTO("Test User", "test@example.com", Role.USER)
        );
        Mockito.when(listAllUsersUseCase.execute()).thenReturn(usersList);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/user/listAll")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Test User"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].email").value("test@example.com"));
    }

    @Test
    @DisplayName("Should return Forbidden for non-ADMIN role")
    @WithMockUser(username = "user", roles = {"USER"})
    public void testListAllUsersForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/user/listAll")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}