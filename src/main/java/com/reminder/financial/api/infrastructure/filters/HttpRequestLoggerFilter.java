package com.reminder.financial.api.infrastructure.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class HttpRequestLoggerFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestLoggerFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        logIncomingRequest(request);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logCompletedRequest(request, response, duration);
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
