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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginUniformResponseTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @MockBean
    private LoginAttemptService loginAttemptService;

    private String login(String email, String password) throws Exception {
        var response = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        return response.replaceAll("\"timestamp\":\"[^\"]*\",", "");
    }

    @Test
    void wrongPasswordUnknownEmailAndDisabledAccountLookIdentical() throws Exception {
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Dan\",\"email\":\"dan@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Off\",\"email\":\"off@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk());
        var off = userRepository.findUserByEmail("off@example.com").orElseThrow();
        off.getAudit().setEnabled(false);
        userRepository.save(off);

        String wrongPassword = login("dan@example.com", "wrong-password");
        assertEquals(wrongPassword, login("nobody@example.com", "wrong-password"));
        assertEquals(wrongPassword, login("off@example.com", "password123"));
    }
}
