package com.reminder.financial.api.infrastructure.security;

import com.reminder.financial.api.domains.models.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

@Configuration
public class AuditorAwareConfig {

    private static final Logger logger = LoggerFactory.getLogger(AuditorAwareConfig.class);

    public static final String SYSTEM_AUDITOR = "system";
    public static final String UNAUTHENTICATED_USER_AUDITOR = "unauthenticated_user";
    public static final String UNEXPECTED_PRINCIPAL_AUDITOR = "system_unexpected_principal";

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return determineAuditor(authentication);
        };
    }

    private Optional<String> determineAuditor(Authentication authentication) {
        if (isSystemAction(authentication)) {
            logger.debug("AuditorAware: System action or no valid authentication context. Auditor: {}", SYSTEM_AUDITOR);
            return Optional.of(SYSTEM_AUDITOR);
        }

        if (isUnauthenticatedUserAction(authentication)) {
            logger.debug("AuditorAware: Unauthenticated user action. Auditor: {}", UNAUTHENTICATED_USER_AUDITOR);
            return Optional.of(UNAUTHENTICATED_USER_AUDITOR);
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            logger.warn("AuditorAware: Principal is null despite valid non-anonymous authentication. Auditor: {}", SYSTEM_AUDITOR);
            return Optional.of(SYSTEM_AUDITOR);
        }

        return getAuditorFromPrincipal(principal);
    }

    private boolean isSystemAction(Authentication authentication) {
        return authentication == null ||
                (!authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken));
    }

    private boolean isUnauthenticatedUserAction(Authentication authentication) {
        if (authentication instanceof AnonymousAuthenticationToken) {
            return true;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            String principalString = (String) principal;
            return "anonymousUser".equals(principalString) || UNAUTHENTICATED_USER_AUDITOR.equals(principalString);
        }
        return false;
    }

    private Optional<String> getAuditorFromPrincipal(Object principal) {

        if (principal instanceof User) {
            String userEmail = ((User) principal).getEmail();
            logger.debug("AuditorAware: Principal is User.");
            return Optional.of(userEmail);
        }

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            logger.debug("AuditorAware: Principal is UserDetails.");
            return Optional.of(username);
        }

        if (principal instanceof String) {
            String principalString = (String) principal;
            logger.debug("AuditorAware: Principal is String (and not anonymous).");
            return Optional.of(principalString);
        }

        logger.warn("AuditorAware: Principal is not an expected type (User, UserDetails, String). Class: {}. Auditor: {}",
                principal.getClass().getName(), UNEXPECTED_PRINCIPAL_AUDITOR);
        return Optional.of(UNEXPECTED_PRINCIPAL_AUDITOR);
    }
}