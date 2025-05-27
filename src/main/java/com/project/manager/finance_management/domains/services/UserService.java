package com.project.manager.finance_management.domains.services;

import com.project.manager.finance_management.domains.models.user.User;
import com.project.manager.finance_management.domains.repositories.jpa.UserRepository;
import com.project.manager.finance_management.infrastructure.midlewares.exceptions.ConflictException;
import com.project.manager.finance_management.infrastructure.midlewares.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository repository;

    public boolean userExists(String email) {
        return repository.findUserByEmail(email).isPresent();
    }

    public User createUser(User user) {
        return repository.save(user);
    }

    public User getUserByEmail(String email) {
        return this.repository.findUserByEmail(email).orElseThrow(
                () -> new NotFoundException("User not found")
        );
    }

    public List<User> listUsers() {
        return this.repository.findAll();
    }
}
