package com.project.manager.finance.management.adapters.usecases.user;

import com.project.manager.finance.management.adapters.dtos.user.CreateUserDTO;
import com.project.manager.finance.management.domains.models.user.User;
import com.project.manager.finance.management.domains.services.UserService;
import com.project.manager.finance.management.infrastructure.midlewares.exceptions.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CreateUserUseCase {
    @Autowired
    private UserService service;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public void execute(CreateUserDTO dto) {
        if(this.service.userExists(dto.email()))
            throw new ConflictException("Email already in use");

        String encryptedPassword = passwordEncoder.encode(dto.password());
        User user = new User(dto.name(), dto.email(), encryptedPassword, dto.role());

        this.service.createUser(user);
    }
}
