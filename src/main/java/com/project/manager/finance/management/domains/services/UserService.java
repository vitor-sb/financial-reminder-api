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

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> getUserByEmail(String email) {
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

}
