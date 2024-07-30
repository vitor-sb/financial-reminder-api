package com.project.manager.finance_management.domains.services;

import com.project.manager.finance_management.domains.models.user.User;
import com.project.manager.finance_management.domains.repositories.UserRepository;
import com.project.manager.finance_management.infrastructure.midlewares.exceptions.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository repository;

    public boolean userExists(String login) {
        return repository.findByLogin(login) != null;
    }

    public User createUser(User user) {
        if(this.userExists(user.getLogin()))
            throw new ConflictException("User with login " + user.getLogin() + " already exists");

        return repository.save(user);
    }

    public User getUserByLogin(String login) {
        return this.repository.findByLogin(login);
    }
}
