package com.project.manager.finance.management.domains.services;

import com.project.manager.finance.management.application.dtos.user.ListUsersFilterQuery;
import com.project.manager.finance.management.domains.models.user.User;
import com.project.manager.finance.management.domains.repositories.jpa.UserRepository;
import com.project.manager.finance.management.infrastructure.utils.SpecificationFactory;
import com.project.manager.finance.management.application.exceptions.ConflictException;
import com.project.manager.finance.management.application.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> getActiveUserByEmail(String email) {
        return userRepository.findUserByEmailAndAuditEnabledTrueAndAuditDeletedFalse(email);
    }

    public Page<User> listUsers(ListUsersFilterQuery query, Pageable pageable) {
        Specification<User> spec = Specification
                .where(SpecificationFactory.<User>like("name", query.name()))
                .and(SpecificationFactory.like("email", query.email()))
                .and(SpecificationFactory.equal("role", query.role()))
                .and(SpecificationFactory.equalInJoin("audit", "enabled", query.enabled()))
                .and(SpecificationFactory.equalInJoin("audit", "deleted", query.deleted()));

        return userRepository.findAll(spec, pageable);
    }

    public User findValidUserByEmailForAuthentication(String email) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found."));

        user.getAudit().validateIsActive("User");

        return user;
    }

    public void checkEmailAvailability(String email) {
        if (userRepository.findUserByEmail(email).isPresent())
            throw new ConflictException("Email already in use.");
    }

    public void toggleUserAccountStatus(String userEmail, boolean enable) {
        User user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found."));

        user.getAudit().setEnabled(enable);
        if (Boolean.TRUE.equals(enable)) {
            user.getAudit().setEnabledAt(Instant.now());
            user.getAudit().setEnabledBy(userEmail);
        }

        userRepository.save(user);
    }

    public void softDeleteUserAccount(String userEmail) {
        User user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found."));

        user.getAudit().setDeleted(true);
        user.getAudit().setDeletedAt(Instant.now());
        user.getAudit().setDeletedBy(userEmail);

        userRepository.save(user);
    }
}
