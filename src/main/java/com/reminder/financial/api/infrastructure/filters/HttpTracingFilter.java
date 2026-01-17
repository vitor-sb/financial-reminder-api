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
public class HttpTracingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(HttpTracingFilter.class);

    private static final String TRACE_ID_KEY = "traceId";
    private static final String HEADER_TRACE_ID = "X-Trace-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String traceId = UUID.randomUUID().toString();

        MDC.put(TRACE_ID_KEY, traceId);

        response.setHeader(HEADER_TRACE_ID, traceId);

        long startTime = System.currentTimeMillis();

        try {
            logIncomingRequest(request);
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logCompletedRequest(request, response, duration);

            MDC.clear();
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