package com.reminder.financial.api.infrastructure.security;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    private final SecurityFilter securityFilter;

    @Value("${docs.enabled:false}")
    private boolean docsEnabled;

    private static final int ONE_YEAR_IN_SECONDS = 60 * 60 * 24 * 365;

    private static final String[] PUBLIC_POST_ENDPOINTS = {
            "/users",
            "/users/me/enable",
            "/auth/login"
    };

    private static final String[] PUBLIC_SWAGGER_ENDPOINTS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private static final String[] PUBLIC_HEALTH_ENDPOINTS = {
            "/actuator/health/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> {
                    authorize
                            .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS).permitAll()
                            .requestMatchers("/admin/**").hasAuthority("ADMIN_PERMISSION");
                    if (docsEnabled) {
                        authorize.requestMatchers(PUBLIC_SWAGGER_ENDPOINTS).permitAll();
                    }
                    authorize
                            .requestMatchers(PUBLIC_HEALTH_ENDPOINTS).permitAll()
                            .anyRequest().authenticated();
                })
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .headers(this::configureHttpHeaders)
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private void configureHttpHeaders(HeadersConfigurer<HttpSecurity> headers) {
        applyContentRelatedHeaders(headers);
        applyTransportSecurityHeaders(headers);
        applyPolicyHeaders(headers);
    }

    private void applyContentRelatedHeaders(HeadersConfigurer<HttpSecurity> headers) {
        headers.contentTypeOptions(Customizer.withDefaults());

        headers.xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK));

        String cspPolicy = "default-src 'self'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "script-src 'self' 'unsafe-inline'; " +
                "object-src 'none'; " +
                "frame-ancestors 'none'; " +
                "form-action 'self'; " +
                "block-all-mixed-content; " +
                "upgrade-insecure-requests;";

        headers.contentSecurityPolicy(csp -> csp.policyDirectives(cspPolicy));

    }

    private void applyTransportSecurityHeaders(HeadersConfigurer<HttpSecurity> headers) {
        headers.httpStrictTransportSecurity(hsts -> hsts
                .includeSubDomains(true)
                .maxAgeInSeconds(ONE_YEAR_IN_SECONDS)
        );
    }

    private void applyPolicyHeaders(HeadersConfigurer<HttpSecurity> headers) {
        headers.referrerPolicy(referrer -> referrer
                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
        );
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

