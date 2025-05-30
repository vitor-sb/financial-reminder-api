package com.project.manager.finance.management.domains.services;

import com.project.manager.finance.management.domains.models.user.User;
import com.project.manager.finance.management.domains.repositories.jpa.UserRepository;
import com.project.manager.finance.management.infrastructure.midlewares.exceptions.ConflictException;
import com.project.manager.finance.management.infrastructure.midlewares.exceptions.NotFoundException;
import com.project.manager.finance.management.infrastructure.midlewares.exceptions.UserDisabledException;
import com.project.manager.finance.management.infrastructure.midlewares.exceptions.UserSoftDeletedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public User findValidUserByEmailForAuthentication(String email) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found."));

        if (Boolean.TRUE.equals(user.getAudit().getDeleted())) {
            throw new UserSoftDeletedException(user.getAudit().getDeletedBy());
        }
        if (Boolean.FALSE.equals(user.getAudit().getEnabled())) {
            throw new UserDisabledException(user.getAudit().getEnabledBy());
        }

        return user;
    }

    public void checkEmailAvailability(String email) {
        if (userRepository.findUserByEmail(email).isPresent())
            throw new ConflictException("Email already in use.");
    }

}
