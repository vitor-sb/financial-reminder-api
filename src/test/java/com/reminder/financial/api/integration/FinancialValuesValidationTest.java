package com.reminder.financial.api.integration;

import com.reminder.financial.api.domains.repositories.jpa.UserRepository;
import com.reminder.financial.api.infrastructure.security.TokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FinancialValuesValidationTest {

    private static final String ID = "00000000-0000-0000-0000-000000000000";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenProvider tokenProvider;

    private void expect400(MockHttpServletRequestBuilder req, String body) throws Exception {
        mockMvc.perform(req.contentType(MediaType.APPLICATION_JSON).content(body)
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isBadRequest());
    }

    private String token() throws Exception {
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"N\",\"email\":\"fin@example.com\",\"password\":\"password123\"}"));
        var user = userRepository.findUserByEmail("fin@example.com").orElseThrow();
        return tokenProvider.generateToken(user);
    }

    private static String reminder(String value, String installments) {
        return "{\"title\":\"t\",\"date\":\"2030-01-01\",\"recurrence\":\"UNICA\",\"method\":\"PIX\",\"value\":"
                + value + ",\"installmentsNumber\":" + installments + "}";
    }

    @Test
    void createReminderRejectsBadValueAndInstallments() throws Exception {
        for (var v : new String[]{"0", "-1", "10.123"}) expect400(post("/payments/reminders"), reminder(v, "1"));
        for (var i : new String[]{"0", "-1", "601"}) expect400(post("/payments/reminders"), reminder("10", i));
    }

    @Test
    void payAndUpdateTransactionRejectBadPaymentValue() throws Exception {
        for (var v : new String[]{"0", "-1", "10.123"}) {
            expect400(post("/payments/transactions/" + ID + "/pay"), "{\"paymentValue\":" + v + "}");
            expect400(patch("/payments/transactions/" + ID), "{\"paymentValue\":" + v + "}");
        }
    }
}
