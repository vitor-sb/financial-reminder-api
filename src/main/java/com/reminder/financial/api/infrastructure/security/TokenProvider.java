package com.reminder.financial.api.infrastructure.security;

import com.reminder.financial.api.application.security.TokenValidator;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.reminder.financial.api.application.security.TokenIssuer;
import com.reminder.financial.api.domains.models.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
public class TokenProvider implements TokenIssuer, TokenValidator {
    @Value("${infrastructure.security.authentication.token.secret-key}")
    private String secretKey;

    @Value("${infrastructure.security.authentication.token.expiration-minutes}")
    private int expirationMinutes;

    @Override
    public String issue(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);

            return JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(user.getUsername())
                    .withJWTId(UUID.randomUUID().toString())
                    .withIssuedAt(Instant.now())
                    .withNotBefore(Instant.now())
                    .withExpiresAt(generateExpirationDate())
                    .sign(algorithm);

        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error while generating token", exception);
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);

            return JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .withClaimPresence("exp")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            throw new BadCredentialsException("Invalid or expired JWT token", exception);
        }
    }

    private Instant generateExpirationDate() {
        return Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES);
    }
}
