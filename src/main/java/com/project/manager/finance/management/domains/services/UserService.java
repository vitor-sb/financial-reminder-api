package com.project.manager.finance.management.domains.services;

import com.project.manager.finance.management.domains.models.user.User;
import com.project.manager.finance.management.domains.repositories.jpa.UserRepository;
import com.project.manager.finance.management.infrastructure.midlewares.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public Optional<User> getUserByEmail(String email) {
        return this.repository.findUserByEmail(email);
    }

    public List<User> listUsers() {
        return this.repository.findAll();
    }
}
