package com.reminder.financial.api.integration;

import com.reminder.financial.api.application.security.LoginAttemptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginIpLimitTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private LoginAttemptService loginAttemptService;

    private org.springframework.test.web.servlet.ResultActions login(String ip, String email) throws Exception {
        return mockMvc.perform(post("/auth/login").with(r -> { r.setRemoteAddr(ip); return r; })
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"wrong-password\"}"));
    }

    @Test
    void blockedIpGets429WithoutTouchingAccountsAndOtherIpIsUnaffected() throws Exception {
        when(loginAttemptService.isIpBlocked("10.0.0.1")).thenReturn(true);
        when(loginAttemptService.ipRetryAfterSeconds("10.0.0.1")).thenReturn(120L);

        login("10.0.0.1", "a@example.com").andExpect(status().isTooManyRequests()).andExpect(header().string("Retry-After", "120"));
        login("10.0.0.1", "b@example.com").andExpect(status().isTooManyRequests());
        verify(loginAttemptService, never()).isBlocked(anyString());

        login("10.0.0.2", "a@example.com").andExpect(status().is4xxClientError());
        verify(loginAttemptService).incrementIpFailures("10.0.0.2");
        verify(loginAttemptService, never()).incrementIpFailures("10.0.0.1");
    }

    @Test
    void forwardedForIsIgnoredByDefault() throws Exception {
        when(loginAttemptService.isIpBlocked("6.6.6.6")).thenReturn(true);
        mockMvc.perform(post("/auth/login").header("X-Forwarded-For", "6.6.6.6")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@example.com\",\"password\":\"x\"}"))
                .andExpect(status().is4xxClientError()).andExpect(status().is(org.hamcrest.Matchers.not(429)));
    }
}
