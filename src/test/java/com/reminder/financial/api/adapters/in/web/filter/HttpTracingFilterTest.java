package com.reminder.financial.api.adapters.in.web.filter;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HttpTracingFilterTest {

    private String run(String header) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/x");
        if (header != null) request.addHeader("X-Trace-Id", header);
        MockHttpServletResponse response = new MockHttpServletResponse();
        new HttpTracingFilter().doFilter(request, response, new MockFilterChain());
        assertNull(MDC.get("traceId"));
        return response.getHeader("X-Trace-Id");
    }

    @Test
    void generatesWhenHeaderMissing() throws Exception {
        assertDoesNotThrow(() -> UUID.fromString(run(null)));
    }

    @Test
    void reusesValidHeader() throws Exception {
        assertEquals("abc-123", run("abc-123"));
    }

    @Test
    void replacesInvalidHeader() throws Exception {
        String bad = "bad id\r\nX: y";
        String out = run(bad);
        assertNotEquals(bad, out);
        assertDoesNotThrow(() -> UUID.fromString(out));
        assertNotEquals("x".repeat(65), run("x".repeat(65)));
    }
}
