package com.reminder.financial.api.domains.services;

import lombok.RequiredArgsConstructor;

import com.reminder.financial.api.domains.filters.UserFilter;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.repositories.UserSearch;
import com.reminder.financial.api.domains.repositories.jpa.UserRepository;
import com.reminder.financial.api.domains.exceptions.ConflictException;
import com.reminder.financial.api.domains.exceptions.NotFoundException;
import com.reminder.financial.api.domains.pagination.PageResult;
import com.reminder.financial.api.domains.pagination.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserSearch userSearch;

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

    public PageResult<User> listUsers(UserFilter query, PageRequest pageable) {
        return userSearch.searchByFilters(query, pageable);
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
