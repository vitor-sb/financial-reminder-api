package com.reminder.financial.api.integration;

import com.reminder.financial.api.application.usecases.paymentreminder.GetPaymentReminderUseCase;
import com.reminder.financial.api.domains.repositories.jpa.UserRepository;
import com.reminder.financial.api.infrastructure.security.TokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
class ErrorLoggingTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenProvider tokenProvider;
    @MockBean
    private GetPaymentReminderUseCase getUseCase;

    private String signUp(String email) throws Exception {
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"N\",\"email\":\"" + email + "\",\"password\":\"password123\"}"));
        return "Bearer " + tokenProvider.issue(userRepository.findUserByEmail(email).orElseThrow());
    }

    @Test
    void serverErrorIsLoggedWithTraceIdAndHidesMessage(CapturedOutput output) throws Exception {
        when(getUseCase.execute(any())).thenThrow(new RuntimeException("secret-internal-detail"));
        String token = signUp("err-log@example.com");

        var res = mockMvc.perform(get("/payments/reminders/abc").header("Authorization", token)).andReturn().getResponse();
        String traceId = res.getHeader("X-Trace-Id");

        assertTrue(res.getContentAsString().contains("Unexpected error"));
        assertFalse(res.getContentAsString().contains("secret-internal-detail"));
        assertTrue(output.getAll().lines().anyMatch(l -> l.contains("ERROR") && l.contains(traceId)));
    }

    @Test
    void pastReminderDateIsBadRequest() throws Exception {
        String token = signUp("past-date@example.com");
        int status = mockMvc.perform(post("/payments/reminders").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"t\",\"date\":\"2000-01-01\",\"recurrence\":\"UNICA\",\"method\":\"PIX\",\"value\":10,\"installmentsNumber\":1}"))
                .andReturn().getResponse().getStatus();
        org.junit.jupiter.api.Assertions.assertEquals(400, status);
    }
}
