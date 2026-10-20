package com.reminder.financial.api.adapters.in.web.filter;

import lombok.RequiredArgsConstructor;

import com.reminder.financial.api.application.security.LoginAttemptService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Limita falhas de credencial por IP nos endpoints que validam senha. Roda antes do controller,
 * então o 429 sai sem consultar o banco. O IP é {@code getRemoteAddr()}; X-Forwarded-For só vale
 * se {@code server.forward-headers-strategy} for habilitado.
 */
@RequiredArgsConstructor
@Component
public class LoginIpRateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> PROTECTED_PATHS = Set.of("/auth/login", "/users/me/enable");

    private final LoginAttemptService loginAttemptService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod()) || !PROTECTED_PATHS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String ip = request.getRemoteAddr();

        if (loginAttemptService.isIpBlocked(ip)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(loginAttemptService.ipRetryAfterSeconds(ip)));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"status\":429,\"error\":\"TOO_MANY_REQUESTS\",\"message\":\"Too many login attempts. Try again later.\"}");
            return;
        }

        chain.doFilter(request, response);

        int status = response.getStatus();
        if (status == HttpStatus.UNAUTHORIZED.value()) {
            loginAttemptService.incrementIpFailures(ip);
        }
    }
}
