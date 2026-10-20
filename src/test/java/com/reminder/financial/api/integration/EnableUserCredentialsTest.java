package com.reminder.financial.api.integration;

import com.reminder.financial.api.domains.repositories.jpa.UserRepository;
import com.reminder.financial.api.domains.services.LoginAttemptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EnableUserCredentialsTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @MockBean
    private LoginAttemptService loginAttemptService;

    private String enable(String email, String password) throws Exception {
        return mockMvc.perform(post("/users/me/enable").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andReturn().getResponse().getContentAsString()
                .replaceAll("\"timestamp\":\"[^\"]*\",", "");
    }

    @Test
    void enableRequiresValidCredentialsAndGivesUniformResponse() throws Exception {
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Eve\",\"email\":\"eve@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk());
        var user = userRepository.findUserByEmail("eve@example.com").orElseThrow();
        user.getAudit().setEnabled(false);
        userRepository.save(user);

        mockMvc.perform(post("/users/me/enable").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"eve@example.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
        assertFalse(userRepository.findUserByEmail("eve@example.com").orElseThrow().getAudit().getEnabled());

        String wrongPassword = enable("eve@example.com", "wrong-password");
        String unknownEmail = enable("nobody@example.com", "wrong-password");
        assertEquals(wrongPassword, unknownEmail);

        mockMvc.perform(post("/users/me/enable").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"eve@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isNoContent());
        assertTrue(userRepository.findUserByEmail("eve@example.com").orElseThrow().getAudit().getEnabled());
    }
}
