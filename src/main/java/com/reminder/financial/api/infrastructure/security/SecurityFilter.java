package com.reminder.financial.api.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reminder.financial.api.adapters.exception.ErrorResponse;
import com.reminder.financial.api.domains.services.AuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    @Autowired
    TokenProvider tokenProvider;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = recoverToken(request);

        if (token != null) {
            try {
                var email = tokenProvider.validateToken(token);
                UserDetails user = authenticationService.loadUserByUsername(email);

                var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                MDC.put("user", email);

            } catch (BadCredentialsException exception) {
                handleInvalidToken(request, response, exception);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void handleInvalidToken(
            HttpServletRequest request,
            HttpServletResponse response,
            BadCredentialsException exception
    ) throws IOException {
        logger.error(
                "JWT Token validation failed for the request [{} {}]: {}",
                request.getMethod(),
                request.getRequestURI(),
                exception.getMessage()
        );

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.name(),
                "Invalid or expired token. Please authenticate again.",
                request.getServletPath()
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;

        return authHeader.substring("Bearer ".length());
    }
}