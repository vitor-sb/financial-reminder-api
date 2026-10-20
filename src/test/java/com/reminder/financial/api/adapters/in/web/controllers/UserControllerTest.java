package com.reminder.financial.api.adapters.in.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reminder.financial.api.adapters.in.web.dtos.user.CreateUserRequest;
import com.reminder.financial.api.application.dtos.user.CreateUserCommand;
import com.reminder.financial.api.domains.filters.UserFilter;
import com.reminder.financial.api.application.dtos.user.ListUsersSingleResult;
import com.reminder.financial.api.application.usecases.user.ListUsersUseCase;
import com.reminder.financial.api.application.usecases.user.CreateUserUseCase;
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

    private CreateUserRequest createUserRequestDTO;

    @BeforeEach
    public void setup() {
        Mockito.reset(createUserUseCase);

        createUserRequestDTO = new CreateUserRequest(
                "Test User",
                "test@example.com",
                "password123"
        );
    }

    @Test
    @DisplayName("Should create a user successfully")
    public void testCreateUserSuccess() throws Exception {
        // Arrange
        Mockito.doNothing().when(createUserUseCase).execute(Mockito.any(CreateUserCommand.class));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequestDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Should return Bad Request for invalid CreateDTO")
    public void testCreaterUserBadRequest() throws Exception {
        // Arrange
        CreateUserRequest invalidDTO = new CreateUserRequest(null, null, null);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}