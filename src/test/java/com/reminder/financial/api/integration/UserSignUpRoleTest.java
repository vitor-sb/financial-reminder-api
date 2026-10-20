package com.reminder.financial.api.integration;

import com.reminder.financial.api.domains.models.user.enums.RoleEnum;
import com.reminder.financial.api.domains.repositories.jpa.UserRepository;
import com.reminder.financial.api.infrastructure.security.TokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserSignUpRoleTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenProvider tokenProvider;

    @Test
    void signUpWithRoleAdminCreatesCommonUserWithoutAdminAccess() throws Exception {
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Mallory\",\"email\":\"mallory@example.com\",\"password\":\"password123\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk());

        var user = userRepository.findUserByEmail("mallory@example.com").orElseThrow();
        assertEquals(RoleEnum.USER, user.getRole());

        mockMvc.perform(get("/admin/users/list")
                        .header("Authorization", "Bearer " + tokenProvider.issue(user)))
                .andExpect(status().isForbidden());
    }
}
