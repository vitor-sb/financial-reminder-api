package com.reminder.financial.api.domains.services;

import lombok.RequiredArgsConstructor;

import com.reminder.financial.api.application.dtos.user.ListUsersFilterQuery;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.repositories.jpa.UserRepository;
import com.reminder.financial.api.infrastructure.utils.AppConstants;
import com.reminder.financial.api.infrastructure.utils.SpecificationFactory;
import com.reminder.financial.api.application.exceptions.ConflictException;
import com.reminder.financial.api.application.exceptions.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public User save(User user) {
        return userRepository.save(user);
    }

    public User findUserByEmailOrThrow(String email) {
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found."));
    }

    public Optional<User> getActiveUserByEmail(String email) {
        return userRepository.findUserByEmailAndAuditEnabledTrueAndAuditDeletedFalse(email);
    }

    public Page<User> listUsers(ListUsersFilterQuery query, Pageable pageable) {
        Instant fromInstant = null;
        if (query.createdFrom() != null) {
            fromInstant = query.createdFrom().atStartOfDay(AppConstants.DEFAULT_ZONE_ID).toInstant();
        }

        Instant untilInstant = null;
        if (query.createdUntil() != null) {
            untilInstant = query.createdUntil().plusDays(1).atStartOfDay(AppConstants.DEFAULT_ZONE_ID).toInstant();
        }

        Specification<User> spec = Specification
                .where(SpecificationFactory.<User>like("name", query.name()))
                .and(SpecificationFactory.like("email", query.email()))
                .and(SpecificationFactory.equal("role", query.role()))
                .and(SpecificationFactory.isGreaterThanOrEqualTo("audit.createdAt", fromInstant))
                .and(SpecificationFactory.isLessThan("audit.createdAt", untilInstant))
                .and(SpecificationFactory.equalInJoin("audit", "enabled", query.enabled()))
                .and(SpecificationFactory.equalInJoin("audit", "deleted", query.deleted()));

        return userRepository.findAll(spec, pageable);
    }

    public User findValidUserByEmail(String email) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found."));

        user.getAudit().validateIsActive("User");

        return user;
    }

    public void checkEmailAvailability(String email) {
        if (userRepository.findUserByEmail(email).isPresent())
            throw new ConflictException("Email already in use.");
    }
}
