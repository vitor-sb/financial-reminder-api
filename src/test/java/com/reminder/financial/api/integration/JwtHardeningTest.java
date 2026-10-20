package com.reminder.financial.api.integration;

import com.auth0.jwt.JWT;
import com.reminder.financial.api.domains.repositories.jpa.UserRepository;
import com.reminder.financial.api.infrastructure.security.TokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JwtHardeningTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenProvider tokenProvider;

    @Test
    void tokenWithoutExactBearerPrefixIsRejectedAndTokenHasJtiIatNbf() throws Exception {
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Jwt\",\"email\":\"jwt@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk());
        var token = tokenProvider.issue(userRepository.findUserByEmail("jwt@example.com").orElseThrow());

        var jwt = JWT.decode(token);
        assertNotNull(jwt.getId());
        assertNotNull(jwt.getIssuedAt());
        assertNotNull(jwt.getNotBefore());

        mockMvc.perform(get("/payments/reminders/list").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/payments/reminders/list").header("Authorization", token))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/payments/reminders/list").header("Authorization", "bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
