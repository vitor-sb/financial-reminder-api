package com.reminder.financial.api.integration;

import com.reminder.financial.api.domains.models.reminder.enums.TransactionStatusEnum;
import com.reminder.financial.api.domains.repositories.jpa.PaymentTransactionRepository;
import com.reminder.financial.api.domains.repositories.jpa.UserRepository;
import com.reminder.financial.api.adapters.out.security.TokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OwnershipUniformResponseTest {

    private static final String MISSING = "00000000-0000-0000-0000-000000000000";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PaymentTransactionRepository transactionRepository;
    @Autowired
    private TokenProvider tokenProvider;

    private String signUp(String email) throws Exception {
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"N\",\"email\":\"" + email + "\",\"password\":\"password123\"}"));
        return "Bearer " + tokenProvider.issue(userRepository.findUserByEmail(email).orElseThrow());
    }

    /** status + body without the timestamp */
    private String call(MockHttpServletRequestBuilder req, String token) throws Exception {
        var res = mockMvc.perform(req.header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andReturn().getResponse();
        return res.getStatus() + res.getContentAsString().replaceAll("\"timestamp\":\"[^\"]*\"", "");
    }

    @Test
    void otherUsersResourceLooksLikeMissingOne() throws Exception {
        String owner = signUp("owner-a@example.com");
        String intruder = signUp("intruder-b@example.com");

        String location = mockMvc.perform(post("/payments/reminders").header("Authorization", owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"t\",\"date\":\"2030-01-01\",\"recurrence\":\"UNICA\",\"method\":\"PIX\",\"value\":10,\"installmentsNumber\":1}"))
                .andReturn().getResponse().getHeader("Location");
        String reminderId = location.substring(location.lastIndexOf('/') + 1);
        String txId = transactionRepository
                .findFirstByReminderIdAndStatusAndAuditDeletedFalse(reminderId, TransactionStatusEnum.PENDENTE)
                .orElseThrow().getId();

        String reminderBase = "/payments/reminders/";
        String txBase = "/payments/transactions/";
        assertEquals(404, Integer.parseInt(call(get(reminderBase + MISSING), intruder).substring(0, 3)));

        for (var path : new String[]{"", "/enable", "/disable"}) {
            var missing = path.isEmpty() ? get(reminderBase + MISSING) : post(reminderBase + MISSING + path);
            var foreign = path.isEmpty() ? get(reminderBase + reminderId) : post(reminderBase + reminderId + path);
            assertEquals(call(missing, intruder), call(foreign, intruder), "reminder " + path);
        }
        assertEquals(call(patch(reminderBase + MISSING), intruder), call(patch(reminderBase + reminderId), intruder));
        assertEquals(call(delete(reminderBase + MISSING), intruder), call(delete(reminderBase + reminderId), intruder));
        assertEquals(call(post(txBase + MISSING + "/pay"), intruder), call(post(txBase + txId + "/pay"), intruder));
        assertEquals(call(patch(txBase + MISSING), intruder), call(patch(txBase + txId), intruder));
    }
}
