package com.project.manager.finance.management.application.usecases.user.impl;

import com.project.manager.finance.management.application.dtos.user.CreateUserCommand;
import com.project.manager.finance.management.application.usecases.user.CreateUserUseCase;
import com.project.manager.finance.management.domains.models.user.User;
import com.project.manager.finance.management.domains.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CreateUserUseCaseImpl implements CreateUserUseCase {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void execute(CreateUserCommand dto) {
        userService.checkEmailAvailability(dto.email());

        String encryptedPassword = passwordEncoder.encode(dto.password());
        User user = new User(dto.name(), dto.email(), encryptedPassword, dto.role());

        userService.save(user);
    }
}
