package com.reminder.financial.api.integration;

import com.reminder.financial.api.domains.models.reminder.enums.TransactionStatusEnum;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.models.user.enums.RoleEnum;
import com.reminder.financial.api.domains.repositories.jpa.PaymentTransactionRepository;
import com.reminder.financial.api.domains.repositories.jpa.UserRepository;
import com.reminder.financial.api.adapters.out.security.TokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

/**
 * Every controller route must appear in {@link #ROUTES} (or {@link #PUBLIC}); a new route without an entry fails
 * {@link #everyRouteIsInTheMatrix()}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthorizationMatrixTest {

    private static final String MISSING = "00000000-0000-0000-0000-000000000000";
    private static final String REMINDER_BODY =
            "{\"title\":\"t\",\"date\":\"2030-01-01\",\"recurrence\":\"UNICA\",\"method\":\"PIX\",\"value\":10,\"installmentsNumber\":1}";

    private static final Set<String> PUBLIC = Set.of("POST /users", "POST /users/me/enable", "POST /auth/login");

    /** route, expected status for the owner (USER) */
    private static final String[][] ROUTES = {
            {"POST /payments/reminders", "201"},
            {"GET /payments/reminders/{id}", "200"},
            {"PATCH /payments/reminders/{id}", "204"},
            {"POST /payments/reminders/{id}/disable", "204"},
            {"POST /payments/reminders/{id}/enable", "204"},
            {"DELETE /payments/reminders/{id}", "204"},
            {"GET /payments/reminders/monthly-bills", "200"},
            {"GET /payments/reminders/list", "200"},
            {"POST /payments/transactions/{transactionId}/pay", "204"},
            {"GET /payments/transactions/list", "200"},
            {"PATCH /payments/transactions/{id}", "204"},
            {"POST /users/me/disable", "204"},
            {"DELETE /users/me", "204"},
            {"GET /admin/users/list", "403"},
    };

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PaymentTransactionRepository transactionRepository;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    static Stream<Object[]> routes() {
        return Stream.of(ROUTES).map(r -> (Object[]) r);
    }

    static Stream<Object[]> idRoutes() {
        return routes().filter(r -> ((String) r[0]).contains("{"));
    }

    private String bearerFor(User user) {
        return "Bearer " + tokenProvider.issue(user);
    }

    private String signUp() throws Exception {
        String email = UUID.randomUUID().toString().substring(0, 8) + "@x.com";
        mockMvc.perform(request(HttpMethod.POST, "/users").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"N\",\"email\":\"" + email + "\",\"password\":\"password123\"}"));
        return bearerFor(userRepository.findUserByEmail(email).orElseThrow());
    }

    private String adminToken() {
        var admin = userRepository.save(new User("A", UUID.randomUUID().toString().substring(0, 8) + "@x.com", "x", RoleEnum.ADMIN));
        return bearerFor(admin);
    }

    /** creates a reminder for the token's user; returns {reminderId, transactionId} */
    private String[] createReminder(String token) throws Exception {
        String location = mockMvc.perform(request(HttpMethod.POST, "/payments/reminders").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(REMINDER_BODY))
                .andReturn().getResponse().getHeader("Location");
        String reminderId = location.substring(location.lastIndexOf('/') + 1);
        String txId = transactionRepository
                .findFirstByReminderIdAndStatusAndAuditDeletedFalse(reminderId, TransactionStatusEnum.PENDENTE)
                .orElseThrow().getId();
        return new String[]{reminderId, txId};
    }

    private int call(String route, String token, String reminderId, String txId) throws Exception {
        String[] parts = route.split(" ");
        String id = parts[1].startsWith("/payments/transactions") ? txId : reminderId;
        MockHttpServletRequestBuilder req = request(HttpMethod.valueOf(parts[0]), parts[1].replaceAll("\\{[^}]+}", id))
                .contentType(MediaType.APPLICATION_JSON)
                .content(parts[0].equals("POST") && parts[1].equals("/payments/reminders") ? REMINDER_BODY.replace("\"t\"", "\"other\"") : "{}");
        if (token != null) {
            req.header("Authorization", token);
        }
        return mockMvc.perform(req).andReturn().getResponse().getStatus();
    }

    @Test
    void everyRouteIsInTheMatrix() {
        Set<String> actual = new TreeSet<>();
        for (var e : handlerMapping.getHandlerMethods().entrySet()) {
            HandlerMethod hm = e.getValue();
            if (!hm.getBeanType().getPackageName().startsWith("com.reminder")) {
                continue;
            }
            RequestMappingInfo info = e.getKey();
            for (var method : info.getMethodsCondition().getMethods()) {
                for (var pattern : info.getPathPatternsCondition().getPatternValues()) {
                    actual.add(method + " " + pattern);
                }
            }
        }
        Set<String> expected = new TreeSet<>(PUBLIC);
        routes().forEach(r -> expected.add((String) r[0]));
        assertEquals(expected, actual);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("routes")
    void anonymousGets401(String route, String status) throws Exception {
        assertEquals(401, call(route, null, MISSING, MISSING));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("routes")
    void userGetsOwnStatusAndAdminOnlyIsForbidden(String route, String status) throws Exception {
        String user = signUp();
        String[] ids = createReminder(user);
        assertEquals(Integer.parseInt(status), call(route, user, ids[0], ids[1]));
    }

    @Test
    void adminReachesAdminRoutes() throws Exception {
        assertEquals(200, call("GET /admin/users/list", adminToken(), MISSING, MISSING));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("idRoutes")
    void otherUsersCannotTouchResourceById(String route, String status) throws Exception {
        String owner = signUp();
        String intruder = signUp();
        String[] ids = createReminder(owner);

        assertEquals(404, call(route, intruder, ids[0], ids[1]));
        // the owner's reminder is still there and active
        assertEquals(200, call("GET /payments/reminders/{id}", owner, ids[0], ids[1]));
    }
}
