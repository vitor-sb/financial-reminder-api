package com.reminder.financial.api.infrastructure.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

// roda antes da cadeia do Spring Security (ordem -100), para o log de JWT inválido já ter traceId
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpTracingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(HttpTracingFilter.class);

    private static final String TRACE_ID_KEY = "traceId";
    private static final String HEADER_TRACE_ID = "X-Trace-Id";
    private static final String USER_KEY = "user";
    private static final Pattern VALID_TRACE_ID = Pattern.compile("[A-Za-z0-9-]{1,64}");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String incoming = request.getHeader(HEADER_TRACE_ID);
        String traceId = incoming != null && VALID_TRACE_ID.matcher(incoming).matches()
                ? incoming : UUID.randomUUID().toString();

        MDC.put(TRACE_ID_KEY, traceId);

        response.setHeader(HEADER_TRACE_ID, traceId);

        long startTime = System.currentTimeMillis();

        try {
            logIncomingRequest(request);
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logCompletedRequest(request, response, duration);

            MDC.remove(TRACE_ID_KEY);
            MDC.remove(USER_KEY);
        }
    }

    private void logIncomingRequest(HttpServletRequest request) {
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        logger.info("Request started: {} {}{}",
                request.getMethod(),
                request.getRequestURI(),
                queryString);
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