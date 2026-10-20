package com.reminder.financial.api.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.reminder.financial.api.application.security.LoginAttemptService;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PasswordPolicyTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private LoginAttemptService loginAttemptService;

    private void signUp(String email, String password, int expected) throws Exception {
        var result = mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"N\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().is(expected));
        if (expected == 400) result.andExpect(content().string(not(containsString(password))));
    }

    @Test
    void signUpRejectsShortLongAndEmailAsPassword() throws Exception {
        signUp("a@example.com", "x", 400);
        signUp("b@example.com", "x".repeat(100), 400);
        signUp("c@example.com", "c@example.com", 400);
        signUp("d@example.com", "validpassword", 200);
    }

    @Test
    void enableRejectsShortAndLongPassword() throws Exception {
        for (var pw : new String[]{"x", "x".repeat(100)}) {
            mockMvc.perform(post("/users/me/enable").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"e@example.com\",\"password\":\"" + pw + "\"}"))
                    .andExpect(status().isBadRequest());
        }
    }
}
