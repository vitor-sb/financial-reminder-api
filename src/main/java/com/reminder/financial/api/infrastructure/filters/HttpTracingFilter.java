package com.reminder.financial.api.infrastructure.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class HttpRequestLoggerFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestLoggerFilter.class);

    private static final String TRACE_ID_KEY = "traceId";
    private static final String SESSION_ID_KEY = "sessionId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        setupMDC(request);

        long startTime = System.currentTimeMillis();

        logIncomingRequest(request);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logCompletedRequest(request, response, duration);

            MDC.clear();
        }
    }

    private void setupMDC(HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        MDC.put(TRACE_ID_KEY, traceId);

        String sessionId = request.getSession(false) != null ? request.getSession(false).getId() : "anon";

        if (sessionId != null) {
            MDC.put(SESSION_ID_KEY, sessionId);
        }
    }

    private void logIncomingRequest(HttpServletRequest request) {
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        logger.info("Request received: {} {}{}",
                request.getMethod(),
                request.getRequestURI(),
                queryString
        );
    }

    private void logCompletedRequest(HttpServletRequest request, HttpServletResponse response, long duration) {
        logger.info("Request completed: {} {} | Status: {} | Duration: {}ms",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration
        );
    }
}